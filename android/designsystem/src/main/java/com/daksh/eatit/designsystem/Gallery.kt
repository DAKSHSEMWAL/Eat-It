package com.daksh.eatit.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EatItGallery(modifier: Modifier = Modifier) {
    var checked by rememberSaveable { mutableStateOf(true) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    if (dialog) EatItConfirmationDialog(
        "Remove saved cart?",
        "You can add dishes again from the menu.",
        "Remove",
        "Keep cart",
        { dialog = false },
        { dialog = false })
    var quantity by rememberSaveable { mutableIntStateOf(1) }
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Eat It / Design system", style = MaterialTheme.typography.headlineLarge)
        Text("Fresh food. Clear choices.", style = MaterialTheme.typography.bodyLarge)
        EatItSectionHeading("01 · Foundations", "Semantic colors, 4dp spacing, scalable typography")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.surfaceVariant
            ).forEach {
                Box(Modifier
                    .size(48.dp)
                    .background(it, MaterialTheme.shapes.medium))
            }
        }
        Text("Made for your appetite", style = MaterialTheme.typography.displaySmall)
        Text("Headline / 28", style = MaterialTheme.typography.headlineMedium)
        Text("Body / 16 · An easy read at every size.", style = MaterialTheme.typography.bodyLarge)
        EatItSectionHeading("02 · Actions")
        EatItButton("Primary action", {})
        EatItButton("Secondary action", {}, style = EatItButtonStyle.Secondary)
        EatItButton("Quiet action", {}, style = EatItButtonStyle.Quiet)
        EatItButton("Unavailable", {}, enabled = false)
        EatItButton("Working", {}, loading = true)
        EatItSectionHeading("03 · Inputs")
        EatItTextField(text, { text = it }, "Delivery address")
        EatItTextField("", {}, "Email", error = "Enter a valid email address")
        EatItSearchField(text, { text = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EatItCategoryChip("All dishes", true, {})
            EatItCategoryChip("Bowls", false, {})
        }
        EatItQuantitySelector(quantity, { quantity = it })
        EatItSectionHeading("04 · Feedback")
        EatItTone.entries.forEach { EatItBadge(it.name, it) }
        EatItLoadingState()
        EatItSectionHeading("Patterns")
        EatItNotice(
            "Menu updated",
            "Review the latest prices before placing your order.",
            tone = EatItTone.Attention
        )
        EatItSetting(
            "Order updates",
            "Keep me informed about this order",
            checked,
            { checked = it })
        EatItButton(
            "Show confirmation dialog",
            { dialog = true },
            style = EatItButtonStyle.Secondary
        )
        EatItOrderProgress(listOf("Placed", "On its way", "Delivered"), 1)
        EatItSectionHeading("05 · Food & commerce")
        EatItFoodCard("Green harvest bowl", "₹249", "Bowls", {}, {}, image = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, null, Modifier.size(56.dp))
            }
        })
        EatItSummaryRow("Subtotal", "₹249")
        EatItSummaryRow("Total", "₹249", true)
        EatItEmptyState(
            "A little room for something good",
            "Your cart is empty. Explore the menu to get started.",
            actionLabel = "Explore menu"
        )
        EatItErrorState("We couldn’t load the menu. Check your connection.", {})
    }
}

@Preview(name = "Light", showBackground = true, widthDp = 390)
@Composable
private fun LightGallery() {
    EatItTheme(false) { Surface { EatItGallery() } }
}

@Preview(name = "Dark", showBackground = true, widthDp = 390)
@Composable
private fun DarkGallery() {
    EatItTheme(true) { Surface { EatItGallery() } }
}

@Preview(name = "Large text", showBackground = true, widthDp = 390, fontScale = 2f)
@Composable
private fun LargeTextGallery() {
    EatItTheme { Surface { EatItGallery() } }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 840, heightDp = 900)
@Composable
private fun TabletGallery() {
    EatItTheme { Surface { EatItGallery() } }
}

@Preview(name = "Compact large text", showBackground = true, widthDp = 320, fontScale = 2f)
@Composable
private fun CompactGallery() {
    EatItTheme { Surface { EatItGallery() } }
}
