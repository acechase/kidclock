package com.acechase.kidclock.quotes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRulesTest {
    private val six = Quote(1, "Be brave.", null, null, listOf("courage"), minAge = 6)
    private val eight = Quote(2, "Adventure is worthwhile in itself.", "Amelia Earhart", null, listOf("adventure"), minAge = 8)
    private val eleven = Quote(3, "Nothing in life is to be feared, it is only to be understood.", "Marie Curie", null, listOf("science"), minAge = 11)
    private val all = listOf(six, eight, eleven)

    @Test fun `the pool shrinks with the oldest kid's age`() {
        assertEquals(listOf(six), QuoteRules.eligible(all, 6))
        assertEquals(listOf(six, eight), QuoteRules.eligible(all, 9))
        assertEquals(all, QuoteRules.eligible(all, 11))
    }

    @Test fun `ages that exclude everything fall back to the whole library`() {
        assertEquals(all, QuoteRules.eligible(all, 3))
    }

    private val lines = listOf(
        LittleLine("You are brave.", listOf("courage")),
        LittleLine("Be brave today.", listOf("courage")),
        LittleLine("Go explore.", listOf("adventure")),
        LittleLine("Today is a good day.", listOf("morning")),
    )

    @Test fun `a little line shares a theme with its quote and never changes`() {
        val first = QuoteRules.littleFor(lines, six)
        assertTrue(first == "You are brave." || first == "Be brave today.")
        assertEquals(first, QuoteRules.littleFor(lines, six))
        assertEquals("Go explore.", QuoteRules.littleFor(lines, eight))
    }

    @Test fun `no shared theme means a morning line, and no lines means none`() {
        assertEquals("Today is a good day.", QuoteRules.littleFor(lines, eleven))
        assertNull(QuoteRules.littleFor(emptyList(), six))
    }
}
