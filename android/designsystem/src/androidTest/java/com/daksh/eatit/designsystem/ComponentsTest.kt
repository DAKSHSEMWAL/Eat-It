package com.daksh.eatit.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loadingActionCannotSubmitAgain() {
        var clicks = 0
        compose.setContent { EatItTheme { EatItButton("Place order", { clicks++ }, loading = true) } }
        compose.onNodeWithText("Place order").assertIsNotEnabled()
        assertEquals(0, clicks)
    }
    @Test fun quantityRespectsBothBounds() {
        compose.setContent {
            var quantity by remember { mutableIntStateOf(0) }
            EatItTheme { EatItQuantitySelector(quantity, { quantity = it }, maximum = 1) }
        }
        compose.onNodeWithContentDescription("Decrease quantity").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Increase quantity").performClick().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Decrease quantity").performClick().assertIsNotEnabled()
    }
    @Test fun disabledFoodCardDisablesBothActions() {
        compose.setContent { EatItTheme {
            EatItFoodCard("Bowl", "249", "Bowls", {}, {}, image = { Text("Image") }, enabled = false)
        } }
        compose.onNodeWithText("Bowl").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Add Bowl to cart").assertIsNotEnabled()
    }
    @Test fun clearSearchEmitsEmptyValue() {
        compose.setContent {
            var query by remember { mutableStateOf("Pizza") }
            EatItTheme { EatItSearchField(query, { query = it }) }
        }
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNodeWithContentDescription("Clear search").assertDoesNotExist()
        compose.onNodeWithText("Search dishes").assertTextContains("")
    }
}
