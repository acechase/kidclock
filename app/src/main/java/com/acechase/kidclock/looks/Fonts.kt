package com.acechase.kidclock.looks

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.acechase.kidclock.R

/**
 * The typefaces the looks draw with, loaded once. The bundled ones live in res/font and are all
 * SIL Open Font License (see FONTS.md at the repo root).
 */
class Fonts private constructor(private val context: Context) {
    val light: Typeface by lazy { Typeface.create("sans-serif-light", Typeface.NORMAL) }
    val regular: Typeface by lazy { Typeface.create("sans-serif", Typeface.NORMAL) }
    val medium: Typeface by lazy { Typeface.create("sans-serif-medium", Typeface.NORMAL) }

    val fredoka: Typeface by lazy { load(R.font.fredoka_semibold) }
    val oswald: Typeface by lazy { load(R.font.oswald_medium) }
    val caveat: Typeface by lazy { load(R.font.caveat_bold) }
    val patrickHand: Typeface by lazy { load(R.font.patrick_hand) }
    val bungeeShade: Typeface by lazy { load(R.font.bungee_shade) }
    val monoton: Typeface by lazy { load(R.font.monoton) }
    val rubikMono: Typeface by lazy { load(R.font.rubik_mono_one) }
    val pressStart: Typeface by lazy { load(R.font.press_start_2p) }

    private fun load(id: Int): Typeface =
        runCatching { ResourcesCompat.getFont(context, id) }.getOrNull() ?: Typeface.DEFAULT

    companion object {
        @Volatile private var instance: Fonts? = null
        fun get(context: Context): Fonts =
            instance ?: Fonts(context.applicationContext).also { instance = it }
    }
}
