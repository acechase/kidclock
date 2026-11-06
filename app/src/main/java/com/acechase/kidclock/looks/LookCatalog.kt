package com.acechase.kidclock.looks

/**
 * The list of looks, in swipe order. A look is the font and layout of the time, date and alarm
 * line; it is independent of the painted background (see faces/). Pure Kotlin so it is testable.
 */
object LookCatalog {
    val names = listOf(
        "Classic", "Bold", "Stacked", "Analog", "Sketch", "Whacky", "Pixel", "Magnets", "Tiara", "Marker", "Marker Stacked",
        "Jersey", "Scoreboard", "Beach", "Bubble", "Plush", "Marker Blue", "Stencil",
        "Tide", "Rainbow Arch", "Nebula", "Glow", "Borealis", "Daisy",
    )
    val count: Int get() = names.size

    const val CLASSIC = 0
    const val ANALOG = 3
    /** How many painted backgrounds there were before looks existed; the old Analog face was stored as the next index. */
    const val BACKGROUNDS_BEFORE_LOOKS = 7

    fun wrap(index: Int): Int = Math.floorMod(index, names.size)

    /**
     * The look each background was drawn for, by background index (see faces/ClockFace.create):
     * Doodle Strings/Tiara, Notebook/Marker Stacked, Soccer Board/Jersey, Stadium/Scoreboard,
     * Crab Tank/Beach, Night Reef/Bubble, Stuffies/Plush, Doodle Notebook/Marker Blue,
     * Blueprint/Stencil, and the skies Ocean/Tide, Rainbow/Rainbow Arch, Galaxy/Nebula,
     * Fireflies/Glow, Aurora/Borealis, Meadow/Daisy. Sky itself suits any look and has no partner.
     */
    private val partners = mapOf(
        1 to 18, 2 to 19, 3 to 20, 4 to 21, 5 to 22, 6 to 23,
        7 to 8, 8 to 10, 9 to 11, 10 to 12, 11 to 13, 12 to 14, 13 to 15, 14 to 16, 15 to 17,
    )

    /** The look that matches the background, or null when the background has no particular one. */
    fun partnerLook(faceIndex: Int): Int? = partners[faceIndex]

    /**
     * The look a swipe to [faceIndex] shows: the background's partner when it has one, else
     * [chosenLook], the look the user last picked in the picker. So the drawn-for backgrounds
     * bring their own look along and the plain skies show whatever the user chose.
     */
    fun lookForBackground(faceIndex: Int, chosenLook: Int): Int = partnerLook(faceIndex) ?: chosenLook

    /**
     * Before looks existed, Analog was stored as the last *background*. Such an install should
     * come back with the Analog look over the first background. Returns the look to switch to,
     * or null when the stored background index needs no migration.
     */
    fun migrateAnalogBackground(faceIndex: Int, backgroundCount: Int): Int? =
        if (faceIndex >= backgroundCount) ANALOG else null
}
