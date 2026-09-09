package com.daksh.eatit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class EatItState(
    val customer: Customer? = null, val catalog: Catalog = Catalog(emptyList(), emptyList()),
    val loading: Boolean = true, val catalogError: String? = null,
    val cart: List<CartLine> = emptyList(), val cartIssues: List<CartIssue> = emptyList(),
    val orders: List<Purchase> = emptyList(),
    val ordersLoading: Boolean = false, val ordersError: String? = null,
    val busy: Boolean = false, val message: String? = null, val placedOrder: String? = null,
) {
    val isStaff: Boolean get() = customer?.role == UserRole.STAFF
}

class EatItViewModel(private val repository: EatItRepository, private val cartStore: CartStore) : ViewModel() {
    private val mutableState = MutableStateFlow(EatItState())
    val state = mutableState.asStateFlow()
    private var catalogJob: Job? = null
    private var ordersJob: Job? = null
    private var checkoutId: String? = null
    private var checkoutDelivery: Delivery? = null
    init {
        viewModelScope.launch {
            repository.customer.collect { user ->
                ordersJob?.cancel()
                val userCart = user?.let { cartStore.read(it.id) }.orEmpty()
                val pendingId = user?.let { cartStore.readPendingRequestId(it.id) }
                checkoutId = pendingId
                mutableState.update { it.copy(customer = user, cart = userCart, cartIssues = emptyList(), orders = emptyList(), placedOrder = null) }
                if (user != null) reloadOrders()
                reloadCatalog()
            }
        }
    }
    fun reloadCatalog() {
        catalogJob?.cancel()
        mutableState.update { it.copy(loading = true, catalogError = null) }
        catalogJob = viewModelScope.launch {
            repository.catalog().catch {
                mutableState.update { it.copy(loading = false, catalogError = "The menu couldn’t be loaded. Check your connection and try again.") }
            }.collect { catalog ->
                val currentCart = state.value.cart
                val user = state.value.customer
                val reconciliation = reconcileCart(currentCart, catalog)
                val updatedIssues = (state.value.cartIssues + reconciliation.issues).distinctBy { it.dishId to it.issueType }

                if (reconciliation.issues.isNotEmpty() && user != null) {
                    cartStore.write(user.id, reconciliation.updatedLines)
                    checkoutId = null
                }

                mutableState.update {
                    it.copy(
                        catalog = catalog,
                        cart = reconciliation.updatedLines,
                        cartIssues = updatedIssues,
                        loading = false
                    )
                }
            }
        }
    }
    fun reloadOrders() {
        ordersJob?.cancel()
        mutableState.update { it.copy(ordersLoading = true, ordersError = null) }
        ordersJob = viewModelScope.launch {
            val ordersFlow = if (state.value.isStaff) repository.staffOrders() else repository.orders()
            ordersFlow.catch {
                mutableState.update { it.copy(ordersLoading = false, ordersError = "Orders couldn’t be loaded.") }
            }.collect { orders -> mutableState.update { it.copy(orders = orders, ordersLoading = false) } }
        }
    }
    fun quantity(dish: Dish, quantity: Int) {
        if (state.value.busy) return
        val user = state.value.customer ?: return
        val cart = updateCart(state.value.cart, dish, quantity.coerceIn(0, 99))
        checkoutId = null
        mutableState.update { it.copy(cart = cart, cartIssues = emptyList()) }
        cartStore.write(user.id, cart)
    }
    fun add(dish: Dish) {
        quantity(dish, (state.value.cart.find { it.dish.id == dish.id }?.quantity ?: 0) + 1)
        mutableState.update { it.copy(message = "${dish.name} added to cart") }
    }
    fun authenticate(email: String, password: String, name: String?) = operation("We couldn’t sign you in. Check your details and connection.") {
        repository.authenticate(email.trim(), password, name?.trim())
    }
    fun resetPassword(email: String) = operation("We couldn’t send the reset email. Check the address and try again.") {
        repository.resetPassword(email.trim())
        mutableState.update { it.copy(message = if (BuildConfig.DEMO) "Demo mode: no email was sent." else "If an account exists, a reset email is on its way.") }
    }
    fun checkout(delivery: Delivery) {
        if (!delivery.valid() || state.value.cart.isEmpty()) return
        if (checkoutDelivery != delivery) checkoutId = null
        checkoutDelivery = delivery
        val user = state.value.customer
        val requestId = checkoutId
            ?: (user?.let { cartStore.readPendingRequestId(it.id) })
            ?: UUID.randomUUID().toString().also { id ->
                checkoutId = id
                user?.let { cartStore.writePendingRequestId(it.id, id) }
            }

        operation("We couldn’t confirm this order. Your cart is saved; retry to check the same order.") {
            try {
                val id = repository.checkout(state.value.cart, delivery, requestId)
                user?.let {
                    cartStore.write(it.id, emptyList())
                    cartStore.clearCheckoutState(it.id)
                }
                mutableState.update { it.copy(cart = emptyList(), cartIssues = emptyList(), placedOrder = id) }
                checkoutId = null
            } catch (e: PriceMismatchException) {
                reloadCatalog()
                mutableState.update { it.copy(message = "Menu prices have updated. Please review your cart before retrying.") }
            } catch (e: ItemUnavailableException) {
                reloadCatalog()
                mutableState.update { it.copy(message = "An item in your cart is no longer available.") }
            }
        }
    }
    fun updateOrderStatus(orderId: String, newStatus: String) = operation("Couldn't update order status.") {
        repository.updateOrderStatus(orderId, newStatus)
        mutableState.update { it.copy(message = "Order status updated to ${orderStatusLabel(newStatus)}") }
    }
    fun updateDish(dish: Dish) = operation("Couldn't update dish.") {
        repository.updateDish(dish)
        mutableState.update { it.copy(message = "${dish.name} updated") }
    }
    fun clearOrder() { mutableState.update { it.copy(placedOrder = null) } }
    fun clearMessage() { mutableState.update { it.copy(message = null) } }
    fun clearCartIssues() { mutableState.update { it.copy(cartIssues = emptyList()) } }
    fun signOut() { if (!state.value.busy) { checkoutId = null; repository.signOut() } }
    private fun operation(error: String, block: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { block() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (e: PriceMismatchException) { mutableState.update { it.copy(message = e.message) } }
            catch (e: ItemUnavailableException) { mutableState.update { it.copy(message = e.message) } }
            catch (_: Exception) { mutableState.update { it.copy(message = error) } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }
}
