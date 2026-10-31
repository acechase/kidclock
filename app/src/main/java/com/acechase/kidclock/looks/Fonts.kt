package com.acechase.kidclock.looks

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.acechase.kidclock.R

/**
 * The typefaces the looks draw with, loaded once. The bundled ones live in res/font and are all
 * SIL Open Font License (see FONTS.md at the repo root; tools/fetch-fonts.sh downloads them).
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

    // The Tiara look and the holiday looks.
    val cormorant: Typeface by lazy { load(R.font.cormorant_garamond_semibold) }
    val greatVibes: Typeface by lazy { load(R.font.great_vibes) }
    val cinzel: Typeface by lazy { load(R.font.cinzel_semibold) }
    val josefin: Typeface by lazy { load(R.font.josefin_sans_light) }
    val playfairItalic: Typeface by lazy { load(R.font.playfair_display_italic_medium) }
    val parisienne: Typeface by lazy { load(R.font.parisienne) }
    val uncial: Typeface by lazy { load(R.font.uncial_antiqua) }
    val comfortaa: Typeface by lazy { load(R.font.comfortaa_bold) }
    val quicksand: Typeface by lazy { load(R.font.quicksand_semibold) }
    val bebas: Typeface by lazy { load(R.font.bebas_neue) }
    val fraunces: Typeface by lazy { load(R.font.fraunces_semibold) }
    val loraItalic: Typeface by lazy { load(R.font.lora_italic_medium) }
    val baskerville: Typeface by lazy { load(R.font.libre_baskerville_bold) }
    val pinyon: Typeface by lazy { load(R.font.pinyon_script) }

    private fun load(id: Int): Typeface =
        runCatching { ResourcesCompat.getFont(context, id) }.getOrNull() ?: Typeface.DEFAULT

    companion object {
        @Volatile private var instance: Fonts? = null
        fun get(context: Context): Fonts =
            instance ?: Fonts(context.applicationContext).also { instance = it }
    }
}
