package com.daksh.eatit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import coil.compose.SubcomposeAsyncImage
import com.daksh.eatit.designsystem.*

@Composable
fun EatItApp(model: EatItViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "menu"
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); model.clearMessage() }
    }
    LaunchedEffect(state.placedOrder) {
        state.placedOrder?.let { nav.navigate("confirmation/$it") { popUpTo("menu") }; model.clearOrder() }
    }
    LaunchedEffect(state.customer?.id) {
        nav.navigate("menu") { popUpTo(nav.graph.id) { inclusive = true }; launchSingleTop = true }
    }
    val mainRoute = route in listOf("menu", "orders", "account", "cart")
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { EatItTopBar(
            when (route) {
                "menu" -> "eat it."; "cart" -> "Your cart"; "orders" -> "Your orders"
                "account" -> "Your space"; "gallery" -> "Design system"; "checkout" -> "Checkout"
                "confirmation/{id}" -> "Order received"; else -> "On the menu"
            },
            onBack = if (!mainRoute && state.customer != null) ({ nav.popBackStack(); Unit }) else null,
            actions = { if (state.customer != null && route != "cart") IconButton({ nav.navigate("cart") { launchSingleTop = true } }) {
                BadgedBox(badge = { if (state.cart.isNotEmpty()) Badge { Text("${state.cart.sumOf { it.quantity }}") } }) {
                    Icon(Icons.Default.ShoppingBag, "Open cart")
                }
            } },
        ) },
        bottomBar = {
            if (mainRoute && state.customer != null) NavigationBar {
                listOf(Triple("menu", "Explore", Icons.Default.RestaurantMenu),
                    Triple("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong),
                    Triple("account", "Account", Icons.Default.PersonOutline)).forEach { (destination, label, icon) ->
                    NavigationBarItem(selected = route == destination, onClick = {
                        nav.navigate(destination) { popUpTo("menu") { saveState = true }; launchSingleTop = true; restoreState = true }
                    }, icon = { Icon(icon, null) }, label = { Text(label) })
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            if (state.customer == null) AuthScreen(state, model)
            else NavHost(nav, "menu", modifier = Modifier.fillMaxSize()) {
                composable("menu") { MenuScreen(state, model, { nav.navigate("dish/$it") }) }
                composable("dish/{id}") { backStack ->
                    val dish = state.catalog.dishes.find { it.id == backStack.arguments?.getString("id") }
                    if (state.loading) EatItLoadingState()
                    else if (dish == null) EatItEmptyState("Dish unavailable", "This dish is no longer on the menu.")
                    else DetailScreen(dish, state, model) { nav.navigate("cart") }
                }
                composable("cart") { CartScreen(state, model, { nav.navigate("menu") }, { nav.navigate("checkout") }) }
                composable("checkout") { CheckoutScreen(state, model) }
                composable("orders") { OrdersScreen(state, model) }
                composable("account") { AccountScreen(state, model) { nav.navigate("gallery") } }
                composable("gallery") { EatItGallery() }
                composable("confirmation/{id}") { backStack ->
                    EatItEmptyState(
                        if (BuildConfig.DEMO) "Demo order saved" else "Thanks. You’re on the list!",
                        "Order ${backStack.arguments?.getString("id").orEmpty().takeLast(8)}. " +
                            if (BuildConfig.DEMO) "This is a preview; no food will be delivered." else "Follow its progress in Your orders.",
                        icon = Icons.Default.CheckCircleOutline, actionLabel = "View orders", onAction = {
                            nav.navigate("orders") { popUpTo("menu") }
                        },
                    )
                }
            }
        }
    }
}
@Composable
private fun MenuScreen(state: EatItState, model: EatItViewModel, onDish: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    val shown = state.catalog.dishes.filter { (category.isEmpty() || it.categoryId == category) && it.name.contains(query, ignoreCase = true) }
    LazyVerticalGrid(GridCells.Adaptive(260.dp), contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (BuildConfig.DEMO) EatItBadge("DEMO · Explore without placing a real order", EatItTone.Attention)
                Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("GOOD FOOD. GOOD MOOD.", style = MaterialTheme.typography.labelLarge)
                        Text("A little fresh.\nA lot to love.", style = MaterialTheme.typography.displaySmall)
                        Text("Find your next favorite, one delicious dish at a time.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                EatItSearchField(query, { query = it })
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { EatItCategoryChip("All dishes", category.isEmpty(), { category = "" }) }
                    items(state.catalog.categories, key = { it.id }) { item -> EatItCategoryChip(item.name, category == item.id, { category = item.id }) }
                }
                EatItSectionHeading(if (query.isEmpty()) "Find your flavor" else "Search results", "${shown.size} dishes to explore")
            }
        }
        when {
            state.loading -> item(span = { GridItemSpan(maxLineSpan) }) { EatItLoadingState() }
            state.catalogError != null -> item(span = { GridItemSpan(maxLineSpan) }) { EatItErrorState(state.catalogError, model::reloadCatalog) }
            shown.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EatItEmptyState("Nothing here yet", "Try another search or category.", actionLabel = "Clear filters", onAction = { query = ""; category = "" })
            }
            else -> items(shown, key = { it.id }) { dish ->
                EatItFoodCard(dish.name, money(dish.pricePaise), state.catalog.categories.find { it.id == dish.categoryId }?.name ?: "Freshly made",
                    { onDish(dish.id) }, { model.add(dish) }, image = { DishImage(dish) })
            }
        }
    }
}
@Composable
private fun DishImage(dish: Dish, modifier: Modifier = Modifier) {
    val fallback: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(when (dish.categoryId) { "pizza" -> Icons.Default.LocalPizza; "burgers" -> Icons.Default.LunchDining; else -> Icons.Default.Restaurant },
                null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
    Box(modifier.fillMaxSize()) {
        if (dish.image.isBlank()) fallback()
        else SubcomposeAsyncImage(dish.image, contentDescription = dish.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            loading = { fallback() }, error = { fallback() })
    }
}
@Composable
private fun DetailScreen(dish: Dish, state: EatItState, model: EatItViewModel, onCart: () -> Unit) {
    Column(Modifier.fillMaxWidth().widthIn(max = 720.dp).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(Modifier.fillMaxWidth().height(280.dp).clip(MaterialTheme.shapes.extraLarge)) { DishImage(dish) }
        EatItBadge(state.catalog.categories.find { it.id == dish.categoryId }?.name ?: "On the menu", EatItTone.Positive)
        Text(dish.name, style = MaterialTheme.typography.headlineLarge)
        Text(money(dish.pricePaise), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        if (dish.description.isNotBlank()) Text(dish.description, style = MaterialTheme.typography.bodyLarge)
        Text("For ingredients and allergens, contact the restaurant before ordering.", style = MaterialTheme.typography.bodyMedium)
        EatItButton("Add to cart", { model.add(dish) }, Modifier.fillMaxWidth())
        if (state.cart.any { it.dish.id == dish.id }) EatItButton("View cart", onCart, Modifier.fillMaxWidth(), style = EatItButtonStyle.Secondary)
    }
}
@Composable
private fun CartScreen(state: EatItState, model: EatItViewModel, onExplore: () -> Unit, onCheckout: () -> Unit) {
    if (state.cart.isEmpty()) { EatItEmptyState("Make room for something good", "Your cart is empty. Add a dish to get started.", actionLabel = "Explore menu", onAction = onExplore); return }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        items(state.cart, key = { it.dish.id }) { line ->
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(line.dish.name, style = MaterialTheme.typography.titleLarge)
                    Text("${money(line.dish.pricePaise)} each", style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        EatItQuantitySelector(line.quantity, { model.quantity(line.dish, it) }, enabled = !state.busy)
                        Text(money(line.totalPaise), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider()
                EatItSummaryRow("Subtotal", money(cartTotal(state.cart)))
                EatItSummaryRow("Delivery", "Included")
                EatItSummaryRow("Total", money(cartTotal(state.cart)), true)
                EatItButton("Continue to checkout", onCheckout, Modifier.fillMaxWidth())
            }
        }
    }
}
@Composable
private fun CheckoutScreen(state: EatItState, model: EatItViewModel) {
    var name by rememberSaveable { mutableStateOf(state.customer?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val delivery = Delivery(name.trim(), phone.trim(), address.trim())
    if (state.cart.isEmpty()) { EatItEmptyState("Your cart is empty", "Add a dish before checking out."); return }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        EatItSectionHeading("Where should we bring it?", "Add your contact details and full delivery address.")
        EatItTextField(name, { name = it }, "Full name", enabled = !state.busy, error = if (attempted && name.trim().length < 2) "Enter your full name" else null)
        EatItTextField(phone, { phone = it }, "Phone number", enabled = !state.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            error = if (attempted && !phone.trim().matches(Regex("\\+?[0-9]{10,15}"))) "Use 10–15 digits, optionally starting with +" else null)
        EatItTextField(address, { address = it }, "Delivery address", enabled = !state.busy, singleLine = false,
            error = if (attempted && address.trim().length < 10) "Include your building, street and area" else null)
        EatItBadge("Pay on delivery", EatItTone.Positive)
        EatItSummaryRow("Total", money(cartTotal(state.cart)), true)
        Text(if (BuildConfig.DEMO) "This is a demo. No payment or delivery will take place." else "Delivery is included. If menu prices change, refresh your cart before retrying.", style = MaterialTheme.typography.bodyMedium)
        EatItButton(if (BuildConfig.DEMO) "Place demo order" else "Place order", { attempted = true; if (delivery.valid()) model.checkout(delivery) },
            Modifier.fillMaxWidth(), loading = state.busy)
    }
}
@Composable
private fun OrdersScreen(state: EatItState, model: EatItViewModel) {
    when {
        state.ordersLoading -> EatItLoadingState()
        state.ordersError != null -> EatItErrorState(state.ordersError, model::reloadOrders)
        state.orders.isEmpty() -> EatItEmptyState("Your next favorite is waiting", "Orders will appear here after checkout.", icon = Icons.AutoMirrored.Filled.ReceiptLong)
        else -> LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.orders, key = { it.id }) { order ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Order ${order.id.takeLast(8)}", style = MaterialTheme.typography.titleLarge)
                        EatItBadge(when (order.status) { "0" -> "Placed"; "1" -> "On its way"; "2" -> "Delivered"; else -> "Status unavailable" }, EatItTone.Positive)
                        Text(order.address)
                        Text(money(order.totalPaise), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
@Composable
private fun AccountScreen(state: EatItState, model: EatItViewModel, onGallery: () -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        EatItSectionHeading(state.customer?.name?.ifBlank { "Welcome back" } ?: "Welcome", state.customer?.email)
        Text("Good food starts with a little curiosity.", style = MaterialTheme.typography.bodyLarge)
        if (BuildConfig.DEMO) EatItBadge("Demo account", EatItTone.Attention)
        if (BuildConfig.DEBUG) EatItButton("Explore design system", onGallery, style = EatItButtonStyle.Secondary)
        EatItButton("Sign out", model::signOut, enabled = !state.busy, style = EatItButtonStyle.Quiet)
    }
}
@Composable
private fun AuthScreen(state: EatItState, model: EatItViewModel) {
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Your appetite.\nOur happy place.", style = MaterialTheme.typography.displaySmall)
        Text(if (register) "Create an account to find your favorites." else "Welcome back. Something delicious is waiting.", style = MaterialTheme.typography.bodyLarge)
        if (BuildConfig.DEMO) EatItBadge("Demo · Use any valid email and 8-character password", EatItTone.Attention)
        if (register) EatItTextField(name, { name = it }, "Name", enabled = !state.busy,
            error = if (attempted && name.trim().length < 2) "Enter your name" else null)
        EatItTextField(email, { email = it }, "Email", enabled = !state.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            error = if (attempted && !emailValid) "Enter a valid email" else null)
        EatItTextField(password, { password = it }, "Password", enabled = !state.busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            error = if (attempted && password.length < 8) "Use at least 8 characters" else null,
            trailingIcon = { IconButton({ visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (visible) "Hide password" else "Show password") } })
        EatItButton(if (register) "Create account" else "Sign in", {
            attempted = true
            if (emailValid && password.length >= 8 && (!register || name.trim().length >= 2)) model.authenticate(email, password, if (register) name else null)
        }, Modifier.fillMaxWidth(), loading = state.busy)
        EatItButton(if (register) "Already a member? Sign in" else "New here? Create account", { register = !register; attempted = false }, enabled = !state.busy, style = EatItButtonStyle.Quiet)
        if (!register) EatItButton("Reset password", { model.resetPassword(email) }, enabled = emailValid && !state.busy, style = EatItButtonStyle.Quiet)
    }
}
