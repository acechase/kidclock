package com.acechase.kidclock.quotes

/** One quote or piece of morning advice. [author] is null for house advice lines. */
data class Quote(
    val id: Int,
    val text: String,
    val author: String?,
    val source: String?,
    val tags: List<String>,
) {
    /** "— Maya Angelou" / "— Merida, Brave" / "— Morning advice". */
    val attribution: String
        get() = when {
            author == null -> "Morning advice"
            source == null -> author
            source.startsWith("by ") -> "$author, $source"
            else -> "$author, $source"
        }
}
