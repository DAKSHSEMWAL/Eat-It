package com.daksh.eatit.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

enum class EatItButtonStyle { Primary, Secondary, Quiet }
@Composable
fun EatItButton(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, loading: Boolean = false, style: EatItButtonStyle = EatItButtonStyle.Primary,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp).semantics { contentDescription = "Loading" },
                color = LocalContentColor.current, strokeWidth = 2.dp)
            Spacer(Modifier.width(EatItTheme.spacing.xs))
        }
        Text(label)
    }
    val sized = modifier.heightIn(min = EatItTheme.sizing.button)
    when (style) {
        EatItButtonStyle.Primary -> Button(onClick, sized, enabled && !loading, content = content)
        EatItButtonStyle.Secondary -> OutlinedButton(onClick, sized, enabled && !loading, content = content)
        EatItButtonStyle.Quiet -> TextButton(onClick, sized, enabled && !loading, content = content)
    }
}
@Composable
fun EatItTextField(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
    error: String? = null, enabled: Boolean = true, singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), enabled = enabled,
        label = { Text(label) }, isError = error != null,
        supportingText = error?.let { { Text(it, Modifier.semantics { liveRegion = LiveRegionMode.Polite }) } },
        singleLine = singleLine, keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation, trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium)
}
@Composable
fun EatItSearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), singleLine = true,
        label = { Text("Search dishes") }, leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { if (value.isNotEmpty()) IconButton({ onValueChange("") }) { Icon(Icons.Default.Close, "Clear search") } },
        shape = MaterialTheme.shapes.large)
}
@Composable
fun EatItCategoryChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(selected, onClick, label = { Text(label) }, modifier = modifier.heightIn(min = 48.dp))
}
@Composable
fun EatItQuantitySelector(quantity: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, maximum: Int = 99) {
    Row(modifier.semantics { isTraversalGroup = true }, verticalAlignment = Alignment.CenterVertically) {
        IconButton({ onChange(quantity - 1) }, enabled = enabled && quantity > 0) {
            Icon(Icons.Default.Remove, "Decrease quantity")
        }
        Text("$quantity", Modifier.padding(horizontal = 8.dp).semantics {
            contentDescription = "Quantity $quantity"; liveRegion = LiveRegionMode.Polite
        }, style = MaterialTheme.typography.titleMedium)
        IconButton({ onChange(quantity + 1) }, enabled = enabled && quantity < maximum) {
            Icon(Icons.Default.Add, "Increase quantity")
        }
    }
}
enum class EatItTone { Neutral, Positive, Attention, Error }
@Composable
fun EatItBadge(label: String, tone: EatItTone = EatItTone.Neutral, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (background, foreground) = when (tone) {
        EatItTone.Neutral -> colors.surfaceVariant to colors.onSurfaceVariant
        EatItTone.Positive -> colors.primaryContainer to colors.onPrimaryContainer
        EatItTone.Attention -> colors.secondaryContainer to colors.onSecondaryContainer
        EatItTone.Error -> colors.errorContainer to colors.onErrorContainer
    }
    Surface(modifier, shape = MaterialTheme.shapes.small, color = background, contentColor = foreground) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}
@Composable
fun EatItSectionHeading(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
@Composable
fun EatItFoodCard(
    name: String, price: String, category: String, onClick: () -> Unit, onAdd: () -> Unit,
    image: @Composable () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
) {
    Card(onClick, modifier, shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Box(Modifier.fillMaxWidth().height(EatItTheme.sizing.foodImage)) { image() }
        Column(Modifier.padding(EatItTheme.spacing.md), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(name, style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(price, style = MaterialTheme.typography.titleMedium)
                FilledTonalIconButton(onAdd, enabled = enabled) { Icon(Icons.Default.Add, "Add $name to cart") }
            }
        }
    }
}
@Composable
fun EatItEmptyState(title: String, message: String, modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Restaurant, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(message, style = MaterialTheme.typography.bodyLarge)
        actionLabel?.let { EatItButton(it, onAction) }
    }
}
@Composable
fun EatItErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EatItEmptyState("Something went wrong", message, modifier, Icons.Default.ErrorOutline, "Try again", onRetry)
}
@Composable
fun EatItLoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = "Loading content" })
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EatItTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { onBack?.let { IconButton(it) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } } },
        actions = actions)
}
@Composable
fun EatItSummaryRow(label: String, value: String, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        Text(value, style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
    }
}
