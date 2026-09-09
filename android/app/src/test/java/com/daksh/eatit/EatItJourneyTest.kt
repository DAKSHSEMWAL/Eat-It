package com.daksh.eatit

import android.content.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EatItJourneyTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeCartStore : CartStore(ContextWrapper(null)) {
        private val storage = mutableMapOf<String, List<CartLine>>()
        private val pending = mutableMapOf<String, String?>()

        override fun read(user: String): List<CartLine> = storage[user] ?: emptyList()
        override fun write(user: String, lines: List<CartLine>) {
            storage[user] = lines
            pending.remove(user)
        }
        override fun readPendingRequestId(user: String): String? = pending[user]
        override fun writePendingRequestId(user: String, requestId: String?) {
            pending[user] = requestId
        }
        override fun clearCheckoutState(user: String) {
            pending.remove(user)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeCustomerJourney() = runTest(testDispatcher) {
        val cartStore = FakeCartStore()
        val repository = FakeRepository()
        val viewModel = EatItViewModel(repository, cartStore)

        testScheduler.advanceUntilIdle()

        val initialState = viewModel.state.value
        assertNotNull(initialState.customer)
        assertEquals("demo@example.com", initialState.customer?.email)

        val dish = DemoCatalog.dishes.first()
        viewModel.add(dish)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.cart.size)
        assertEquals(dish, viewModel.state.value.cart[0].dish)

        val delivery = Delivery("Daksh", "+12345678901", "123 Main Street, Apt 4B")
        viewModel.checkout(delivery)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.cart.isEmpty())
        assertNotNull(viewModel.state.value.placedOrder)

        val orders = viewModel.state.value.orders
        assertFalse(orders.isEmpty())
        assertEquals("0", orders.first().status)
    }

    private class FakeRepository : EatItRepository {
        override val customer = MutableStateFlow<Customer?>(
            Customer("demo", "Food lover", "demo@example.com")
        )
        private val purchases = MutableStateFlow<List<Purchase>>(emptyList())

        override fun catalog() = flowOf(DemoCatalog)
        override fun orders() = purchases
        override fun staffOrders() = purchases

        override suspend fun authenticate(email: String, password: String, name: String?) {
            customer.value = Customer("demo", name ?: "Food lover", email)
        }
        override suspend fun resetPassword(email: String) = Unit
        override suspend fun checkout(lines: List<CartLine>, delivery: Delivery, requestId: String): String {
            purchases.value = listOf(Purchase(requestId, cartTotal(lines), "0", delivery.address)) + purchases.value
            return requestId
        }
        override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
            purchases.value = purchases.value.map { if (it.id == orderId) it.copy(status = newStatus) else it }
        }
        override suspend fun updateDish(dish: Dish) = Unit
        override fun signOut() { customer.value = null }
    }
}
