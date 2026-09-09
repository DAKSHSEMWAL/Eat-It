package com.daksh.eatit.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Destination identity belongs to the caller; this module has no navigation dependency. */
@Immutable
data class EatItDestination(val id: String, val label: String, val icon: ImageVector)

/** Uses available window width, so split-screen windows behave like compact devices. */
@Composable
fun EatItNavigationLayout(
    destinations: List<EatItDestination>, selected: String, onSelect: (String) -> Unit,
    modifier: Modifier = Modifier, visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (!visible) content()
        else if (maxWidth >= 600.dp) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    destinations.forEach { destination ->
                        NavigationRailItem(selected == destination.id, { onSelect(destination.id) },
                            icon = { Icon(destination.icon, null) }, label = { Text(destination.label) })
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(selected == destination.id, { onSelect(destination.id) },
                            icon = { Icon(destination.icon, null) }, label = { Text(destination.label) })
                    }
                }
            }
        }
    }
}

/** A bounded, centered surface for forms and reading; callers own scrolling. */
@Composable
fun EatItContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = EatItTheme.sizing.readingWidth).fillMaxWidth()
            .padding(EatItTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(EatItTheme.spacing.md),
            content = content)
    }
}

@Composable
fun EatItNotice(title: String, message: String, modifier: Modifier = Modifier,
    tone: EatItTone = EatItTone.Neutral, action: @Composable (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    val (background, foreground) = when (tone) {
        EatItTone.Neutral -> colors.surfaceContainerHigh to colors.onSurface
        EatItTone.Positive -> colors.primaryContainer to colors.onPrimaryContainer
        EatItTone.Attention -> colors.secondaryContainer to colors.onSecondaryContainer
        EatItTone.Error -> colors.errorContainer to colors.onErrorContainer
    }
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        color = background, contentColor = foreground) {
        Column(Modifier.padding(EatItTheme.spacing.md), verticalArrangement = Arrangement.spacedBy(EatItTheme.spacing.xs)) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            action?.invoke()
        }
    }
}

@Composable
fun EatItSetting(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    // The switch is the single accessible toggle target, labeled independently of its visual position.
    ListItem(modifier = modifier, headlineContent = { Text(label) }, supportingContent = { Text(description) },
        trailingContent = { Switch(checked, onCheckedChange, enabled = enabled,
            modifier = Modifier.semantics { contentDescription = label }) })
}

@Composable
fun EatItConfirmationDialog(title: String, message: String, confirmLabel: String, dismissLabel: String,
    onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) },
        confirmButton = { EatItButton(confirmLabel, onConfirm) },
        dismissButton = { EatItButton(dismissLabel, onDismiss, style = EatItButtonStyle.Quiet) })
}

@Composable
fun EatItOrderProgress(steps: List<String>, currentStep: Int, modifier: Modifier = Modifier) {
    require(steps.isNotEmpty() && currentStep in steps.indices)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(EatItTheme.spacing.sm)) {
        steps.forEachIndexed { index, label ->
            val status = stringResource(if (index < currentStep) R.string.eatit_complete else if (index == currentStep) R.string.eatit_current else R.string.eatit_upcoming)
            EatItBadge("${index + 1}. $label", if (index <= currentStep) EatItTone.Positive else EatItTone.Neutral,
                Modifier.semantics { stateDescription = status })
        }
    }
}
