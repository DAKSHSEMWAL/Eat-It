package com.daksh.eatit.designsystem

import org.junit.Assert.*
import org.junit.Test

class DesignSystemGovernanceTest {

    @Test
    fun verifyButtonStylesEnum() {
        val styles = EatItButtonStyle.values()
        assertEquals(3, styles.size)
        assertTrue(styles.contains(EatItButtonStyle.Primary))
        assertTrue(styles.contains(EatItButtonStyle.Secondary))
        assertTrue(styles.contains(EatItButtonStyle.Quiet))
    }

    @Test
    fun verifyToneEnum() {
        val tones = EatItTone.values()
        assertEquals(4, tones.size)
        assertTrue(tones.contains(EatItTone.Neutral))
        assertTrue(tones.contains(EatItTone.Positive))
        assertTrue(tones.contains(EatItTone.Attention))
        assertTrue(tones.contains(EatItTone.Error))
    }

    @Test
    fun verifyThemeSpacingAndSizingTokens() {
        val spacing = EatItSpacing()
        assertTrue(spacing.xs < spacing.sm)
        assertTrue(spacing.sm < spacing.md)
        assertTrue(spacing.md < spacing.lg)
        assertTrue(spacing.lg < spacing.xl)

        val sizing = EatItSizing()
        assertTrue(sizing.button >= spacing.xl)
        assertTrue(sizing.foodImage >= sizing.button)
    }
}
