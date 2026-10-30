package com.acechase.kidclock.looks

/**
 * The list of looks, in swipe order. A look is the font and layout of the time, date and alarm
 * line; it is independent of the painted background (see faces/). Pure Kotlin so it is testable.
 */
object LookCatalog {
    val names = listOf("Classic", "Bold", "Stacked", "Analog", "Sketch", "Whacky", "Pixel", "Magnets")
    val count: Int get() = names.size

    const val CLASSIC = 0
    const val ANALOG = 3

    fun wrap(index: Int): Int = Math.floorMod(index, names.size)

    /**
     * Before looks existed, Analog was stored as the last *background*. Such an install should
     * come back with the Analog look over the first background. Returns the look to switch to,
     * or null when the stored background index needs no migration.
     */
    fun migrateAnalogBackground(faceIndex: Int, backgroundCount: Int): Int? =
        if (faceIndex >= backgroundCount) ANALOG else null
}
