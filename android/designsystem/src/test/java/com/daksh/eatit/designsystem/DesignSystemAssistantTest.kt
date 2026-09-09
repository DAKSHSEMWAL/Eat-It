package com.daksh.eatit.designsystem

import com.daksh.eatit.designsystem.assistant.AssistantResponse
import com.daksh.eatit.designsystem.assistant.DesignSystemAssistant
import org.junit.Assert.*
import org.junit.Test

class DesignSystemAssistantTest {

    @Test
    fun testValidComponentQueryReturnsCitation() {
        val response = DesignSystemAssistant.answerQuery("button")
        assertTrue(response is AssistantResponse.Found)
        val found = response as AssistantResponse.Found
        assertEquals("EatItButton", found.component.name)
        assertTrue(found.component.fileCitation.endsWith("Components.kt"))
    }

    @Test
    fun testValidBadgeQueryReturnsCitation() {
        val response = DesignSystemAssistant.answerQuery("EatItBadge")
        assertTrue(response is AssistantResponse.Found)
        val found = response as AssistantResponse.Found
        assertEquals("EatItBadge", found.component.name)
        assertTrue(found.summary.contains("Positive"))
    }

    @Test
    fun testInvalidOrInventedComponentIsRejected() {
        val response = DesignSystemAssistant.answerQuery("EatItCarousel")
        assertTrue(response is AssistantResponse.Rejected)
        val rejected = response as AssistantResponse.Rejected
        assertEquals("EatItCarousel", rejected.query)
        assertTrue(rejected.availableComponents.contains("EatItButton"))
    }
}
