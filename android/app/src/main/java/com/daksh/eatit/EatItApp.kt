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
import com.daksh.eatit.search.DeterministicSearchEngine

@Composable
fun EatItApp(model: EatItViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    key(state.customer?.id) { EatItSession(state, model) }
}

@Composable
private fun EatItSession(state: EatItState, model: EatItViewModel) {
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
    val mainRoute = route in listOf("menu", "orders", "account", "cart", "staff_catalog")
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { EatItTopBar(
            when (route) {
                "menu" -> if (state.isStaff) "Staff Workspace" else "eat it."
                "cart" -> "Your cart"; "orders" -> if (state.isStaff) "Kitchen Queue" else "Your orders"
                "staff_catalog" -> "Manage Catalog"
                "account" -> "Your space"; "gallery" -> "Design system"; "checkout" -> "Checkout"
                "confirmation/{id}" -> "Order received"; else -> "On the menu"
            },
            onBack = if (!mainRoute && state.customer != null) ({ nav.popBackStack(); Unit }) else null,
            actions = {
                if (state.customer != null && !state.isStaff && route != "cart") IconButton({ nav.navigate("cart") { launchSingleTop = true } }) {
                    BadgedBox(badge = { if (state.cart.isNotEmpty()) Badge { Text("${state.cart.sumOf { it.quantity }}") } }) {
                        Icon(Icons.Default.ShoppingBag, "Open cart")
                    }
                }
            },
        ) },
    ) { padding ->
        val destinations = if (state.isStaff) listOf(
            EatItDestination("orders", "Kitchen Queue", Icons.AutoMirrored.Filled.ReceiptLong),
            EatItDestination("staff_catalog", "Edit Catalog", Icons.Default.Edit),
            EatItDestination("account", "Account", Icons.Default.PersonOutline)
        ) else listOf(
            EatItDestination("menu", "Explore", Icons.Default.RestaurantMenu),
            EatItDestination("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong),
            EatItDestination("account", "Account", Icons.Default.PersonOutline)
        )

        EatItNavigationLayout(
            destinations = destinations,
            selected = route, visible = mainRoute && state.customer != null,
            onSelect = { destination -> nav.navigate(destination) {
                popUpTo(if (state.isStaff) "orders" else "menu") { saveState = true }; launchSingleTop = true; restoreState = true
            } }, modifier = Modifier.padding(padding).imePadding(),
        ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            if (state.customer == null) AuthScreen(state, model)
            else NavHost(nav, if (state.isStaff) "orders" else "menu", modifier = Modifier.fillMaxSize()) {
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
                composable("staff_catalog") { StaffCatalogEditScreen(state, model) }
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
}
@Composable
private fun MenuScreen(state: EatItState, model: EatItViewModel, onDish: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    val searched = DeterministicSearchEngine.search(query, state.catalog.dishes, state.catalog.categories)
    val shown = searched.filter { (category.isEmpty() || it.categoryId == category) }
    LazyVerticalGrid(GridCells.Adaptive(260.dp), contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (BuildConfig.DEMO) EatItBadge("DEMO · Explore without placing a real order", EatItTone.Attention)
                    if (state.catalog.isOffline) EatItBadge("Offline catalog cache", EatItTone.Attention)
                    else if (state.catalog.lastSyncedTimestamp > 0L) EatItBadge("Updated catalog", EatItTone.Positive)
                }
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
    Column(Modifier.widthIn(max = EatItTheme.sizing.readingWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(Modifier.fillMaxWidth().height(280.dp).clip(MaterialTheme.shapes.extraLarge)) { DishImage(dish) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            EatItBadge(state.catalog.categories.find { it.id == dish.categoryId }?.name ?: "On the menu", EatItTone.Positive)
            IconButton({ model.toggleFavorite(dish.id) }) {
                Icon(if (state.isFavorite(dish.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite",
                    tint = if (state.isFavorite(dish.id)) MaterialTheme.colorScheme.primary else LocalContentColor.current)
            }
        }
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
        if (state.cartIssues.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.cartIssues.forEach { issue ->
                        val text = when (issue.issueType) {
                            CartIssueType.PRICE_CHANGED -> "Price for ${issue.dishName} changed from ${money(issue.oldPricePaise)} to ${money(issue.newPricePaise)}."
                            CartIssueType.REMOVED -> "${issue.dishName} is no longer available and was removed from your cart."
                        }
                        EatItNotice("Menu Update", text, tone = EatItTone.Attention)
                    }
                    EatItButton("Acknowledge updates", model::clearCartIssues, style = EatItButtonStyle.Quiet)
                }
            }
        }
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
        state.orders.isEmpty() -> EatItEmptyState(
            if (state.isStaff) "Kitchen Queue Empty" else "Your next favorite is waiting",
            if (state.isStaff) "New customer orders will appear here." else "Orders will appear here after checkout.",
            icon = Icons.AutoMirrored.Filled.ReceiptLong
        )
        else -> LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.orders, key = { it.id }) { order ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Order ${order.id.takeLast(8)}", style = MaterialTheme.typography.titleLarge)
                        val step = order.status.toIntOrNull()
                        if (step != null && step in 0..2) EatItOrderProgress(listOf("Placed", "On its way", "Delivered"), step)
                        else EatItBadge("Status unavailable")
                        Text(order.address)
                        Text(money(order.totalPaise), style = MaterialTheme.typography.titleMedium)

                        if (state.isStaff) {
                            val next = nextOrderStatus(order.status)
                            if (next != null) {
                                EatItButton("Advance to ${orderStatusLabel(next)}", {
                                    model.updateOrderStatus(order.id, next)
                                }, enabled = !state.busy)
                            } else {
                                EatItBadge("Completed", EatItTone.Positive)
                            }
                        } else {
                            EatItButton("Reorder", { model.reorder(order) }, style = EatItButtonStyle.Secondary)
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun StaffCatalogEditScreen(state: EatItState, model: EatItViewModel) {
    var selectedDish by remember { mutableStateOf<Dish?>(null) }

    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            EatItSectionHeading("Menu Catalog Management", "Tap a dish to update its details or pricing.")
        }
        items(state.catalog.dishes, key = { it.id }) { dish ->
            Card(onClick = { selectedDish = dish }) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(dish.name, style = MaterialTheme.typography.titleMedium)
                        Text(money(dish.pricePaise), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Default.Edit, "Edit dish")
                }
            }
        }
    }

    selectedDish?.let { dish ->
        var name by remember(dish) { mutableStateOf(dish.name) }
        var priceInput by remember(dish) { mutableStateOf((dish.pricePaise / 100.0).toString()) }
        var description by remember(dish) { mutableStateOf(dish.description) }

        AlertDialog(
            onDismissRequest = { selectedDish = null },
            title = { Text("Edit ${dish.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EatItTextField(name, { name = it }, "Dish Name")
                    EatItTextField(priceInput, { priceInput = it }, "Price (₹)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    EatItTextField(description, { description = it }, "Description", singleLine = false)
                }
            },
            confirmButton = {
                EatItButton("Save changes", {
                    val parsed = parsePrice(priceInput)
                    if (parsed != null && name.isNotBlank()) {
                        model.updateDish(dish.copy(name = name.trim(), pricePaise = parsed, description = description.trim()))
                        selectedDish = null
                    }
                })
            },
            dismissButton = {
                EatItButton("Cancel", { selectedDish = null }, style = EatItButtonStyle.Quiet)
            }
        )
    }
}
@Composable
private fun AccountScreen(state: EatItState, model: EatItViewModel, onGallery: () -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        EatItSectionHeading(state.customer?.name?.ifBlank { "Welcome back" } ?: "Welcome", state.customer?.email)
        Text("Good food starts with a little curiosity.", style = MaterialTheme.typography.bodyLarge)
        if (state.isStaff) EatItBadge("Staff Workspace Access", EatItTone.Positive)
        else if (BuildConfig.DEMO) EatItBadge("Demo account", EatItTone.Attention)
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
        if (BuildConfig.DEMO) EatItBadge("Demo · Use staff@example.com for Staff Mode", EatItTone.Attention)
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
