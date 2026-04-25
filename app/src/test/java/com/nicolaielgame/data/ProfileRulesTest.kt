package com.nicolaielgame.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRulesTest {
    @Test
    fun sanitizedSlot_clampsToLocalProfileRange() {
        assertEquals(1, ProfileRules.sanitizedSlot(0))
        assertEquals(2, ProfileRules.sanitizedSlot(2))
        assertEquals(3, ProfileRules.sanitizedSlot(99))
    }
}
