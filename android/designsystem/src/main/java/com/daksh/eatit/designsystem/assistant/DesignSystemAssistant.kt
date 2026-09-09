package com.daksh.eatit.designsystem.assistant

data class DesignSystemComponent(
    val name: String,
    val signature: String,
    val description: String,
    val fileCitation: String,
    val usageExample: String
)

sealed class AssistantResponse {
    data class Found(
        val component: DesignSystemComponent,
        val summary: String
    ) : AssistantResponse()

    data class Rejected(
        val query: String,
        val reason: String,
        val availableComponents: List<String>
    ) : AssistantResponse()
}

object DesignSystemAssistant {

    private val components = listOf(
        DesignSystemComponent(
            name = "EatItButton",
            signature = "fun EatItButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false, style: EatItButtonStyle = EatItButtonStyle.Primary)",
            description = "Primary, Secondary, or Quiet button with built-in loading indicator and accessibility semantics.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItButton(\"Add to cart\", onClick = { model.add(dish) })"
        ),
        DesignSystemComponent(
            name = "EatItTextField",
            signature = "fun EatItTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, error: String? = null, enabled: Boolean = true, singleLine: Boolean = true)",
            description = "Outlined text field supporting polite live-region error messages, visual transformations, and trailing icons.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItTextField(name, onValueChange = { name = it }, label = \"Full name\", error = errorText)"
        ),
        DesignSystemComponent(
            name = "EatItSearchField",
            signature = "fun EatItSearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier)",
            description = "Outlined search input with search icon leading affordance and clear button.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItSearchField(query, onValueChange = { query = it })"
        ),
        DesignSystemComponent(
            name = "EatItCategoryChip",
            signature = "fun EatItCategoryChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)",
            description = "Filter chip for menu categories with minimum 48dp touch target.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItCategoryChip(\"Pizza\", selected = isPizzaSelected, onClick = { selectCategory(\"pizza\") })"
        ),
        DesignSystemComponent(
            name = "EatItQuantitySelector",
            signature = "fun EatItQuantitySelector(quantity: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, maximum: Int = 99)",
            description = "Accessible quantity increment/decrement controls with live-region announcement.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItQuantitySelector(line.quantity, onChange = { updateQty(it) })"
        ),
        DesignSystemComponent(
            name = "EatItBadge",
            signature = "fun EatItBadge(label: String, tone: EatItTone = EatItTone.Neutral, modifier: Modifier = Modifier)",
            description = "Compact semantic status badge with Neutral, Positive, Attention, or Error tones.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Components.kt",
            usageExample = "EatItBadge(\"Pay on delivery\", tone = EatItTone.Positive)"
        ),
        DesignSystemComponent(
            name = "EatItNotice",
            signature = "fun EatItNotice(title: String, message: String, modifier: Modifier = Modifier, tone: EatItTone = EatItTone.Neutral)",
            description = "Bounded container surface for important system and cart notices.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Patterns.kt",
            usageExample = "EatItNotice(\"Menu Update\", \"Price changed from ₹200 to ₹220\", tone = EatItTone.Attention)"
        ),
        DesignSystemComponent(
            name = "EatItNavigationLayout",
            signature = "fun EatItNavigationLayout(destinations: List<EatItDestination>, selected: String, onSelect: (String) -> Unit)",
            description = "Adaptive layout providing NavigationRail on wide windows (>=600dp) and NavigationBar on compact screens.",
            fileCitation = "designsystem/src/main/java/com/daksh/eatit/designsystem/Patterns.kt",
            usageExample = "EatItNavigationLayout(destinations, selected = currentRoute, onSelect = { navigate(it) })"
        )
    )

    fun answerQuery(query: String): AssistantResponse {
        val trimmed = query.trim().lowercase()
        val match = components.find {
            it.name.lowercase() == trimmed ||
            it.name.lowercase().contains(trimmed) ||
            trimmed.contains(it.name.lowercase().removePrefix("eatit"))
        }

        return if (match != null) {
            AssistantResponse.Found(
                component = match,
                summary = "${match.name}: ${match.description}\nCitation: ${match.fileCitation}\nSignature: `${match.signature}`"
            )
        } else {
            AssistantResponse.Rejected(
                query = query,
                reason = "No matching component found in the Eat-It design system.",
                availableComponents = components.map { it.name }
            )
        }
    }
}
