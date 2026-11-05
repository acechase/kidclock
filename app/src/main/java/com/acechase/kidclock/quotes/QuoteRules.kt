package com.acechase.kidclock.quotes

/** A few easy words for an early reader, shown over the morning quote. Tagged with the themes it suits. */
data class LittleLine(val text: String, val tags: List<String>)

/**
 * The pure rules behind the quote library: which quotes suit the kids using the clock, and which little line
 * goes over a quote. Kept free of Android so they are unit-tested.
 */
object QuoteRules {
    /**
     * Quotes written for the oldest kid's age or younger. Every quote carries a minimum age; the pool shrinks for
     * younger kids and is everything for an 11-year-old. Falls back to all of them if the ages exclude everything.
     */
    fun eligible(quotes: List<Quote>, oldestAge: Int): List<Quote> =
        quotes.filter { it.minAge <= oldestAge }.ifEmpty { quotes }

    /**
     * The little line to pair with [q]: one sharing a theme with it, chosen by the quote's id so the same quote
     * always gets the same line; a morning line when no theme matches; null with no lines at all.
     */
    fun littleFor(lines: List<LittleLine>, q: Quote): String? {
        if (lines.isEmpty()) return null
        val themed = lines.filter { l -> l.tags.any { it in q.tags } }
        val pool = themed.ifEmpty { lines.filter { "morning" in it.tags } }.ifEmpty { lines }
        return pool[Math.floorMod(q.id, pool.size)].text
    }
}
