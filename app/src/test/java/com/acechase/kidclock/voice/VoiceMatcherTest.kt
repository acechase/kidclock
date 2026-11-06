package com.acechase.kidclock.voice

import com.acechase.kidclock.voice.VoiceMatcher.Command.DISMISS
import com.acechase.kidclock.voice.VoiceMatcher.Command.SNOOZE
import com.acechase.kidclock.voice.VoiceMatcher.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceMatcherTest {
    private fun w(vararg pairs: Pair<String, Double>) = pairs.map { Word(it.first, it.second) }

    @Test
    fun aClearGrammarPhraseIsACommand() {
        assertEquals(SNOOZE, VoiceMatcher.match(w("snooze" to 0.95), 5_000L, false))
        assertEquals(DISMISS, VoiceMatcher.match(w("stop" to 0.8), 5_000L, false))
        assertEquals(DISMISS, VoiceMatcher.match(w("i'm" to 0.8, "awake" to 0.8), 5_000L, false))
        assertEquals(DISMISS, VoiceMatcher.match(w("turn" to 0.75, "off" to 0.9, "alarm" to 0.7), 5_000L, false))
    }

    @Test
    fun theFirstMomentAndSilenceAndUnknownWordsAreIgnored() {
        assertNull(VoiceMatcher.match(w("stop" to 0.99), 1_000L, false))
        assertNull(VoiceMatcher.match(emptyList(), 5_000L, false))
        assertNull(VoiceMatcher.match(w("[unk]" to 0.0), 5_000L, false))
        assertNull(VoiceMatcher.match(w("off" to 0.95, "[unk]" to 0.0), 5_000L, false))   // "off" inside other talk
        assertNull(VoiceMatcher.match(w("stop" to 0.5), 5_000L, false))                     // not clear enough
        assertNull(VoiceMatcher.match(w("i'm" to 0.8, "alarm" to 0.8), 5_000L, false))     // not a phrase
    }

    @Test
    fun inTheWakeWindowSnoozeMeansNothingAndOneWordMustBeVeryClear() {
        assertNull(VoiceMatcher.match(w("snooze" to 0.99), 5_000L, true))
        assertNull(VoiceMatcher.match(w("off" to 0.80), 5_000L, true))
        assertEquals(DISMISS, VoiceMatcher.match(w("off" to 0.95), 5_000L, true))
        assertEquals(DISMISS, VoiceMatcher.match(w("stop" to 0.80, "alarm" to 0.75), 5_000L, true))
    }

    @Test
    fun aQuickImUpIsHeardAsItsWordsAndStillCounts() {
        // What the Portal's log showed for "I'm up!": the short "up" at middling confidence, the "I'm" often lost.
        assertEquals(DISMISS, VoiceMatcher.match(w("am" to 0.9, "up" to 0.51), 5_000L, true))
        assertEquals(DISMISS, VoiceMatcher.match(w("i'm" to 0.6, "up" to 0.5), 5_000L, true))
        assertEquals(DISMISS, VoiceMatcher.match(w("i" to 0.7, "am" to 0.7, "up" to 0.45), 5_000L, false))
        assertEquals(DISMISS, VoiceMatcher.match(w("am" to 0.8, "awake" to 0.6), 5_000L, true))
        // Other talk around it is fine ("I'm up now!"), not inside it ("I am not up").
        assertEquals(DISMISS, VoiceMatcher.match(w("am" to 0.9, "up" to 0.5, "[unk]" to 0.0), 5_000L, true))
        assertEquals(DISMISS, VoiceMatcher.match(w("[unk]" to 0.0, "i'm" to 0.6, "up" to 0.5), 5_000L, true))
        assertNull(VoiceMatcher.match(w("am" to 0.9, "[unk]" to 0.0, "up" to 0.9), 5_000L, true))
    }

    @Test
    fun aLoneUpOrAmOrAFaintOneStaysANo() {
        assertNull(VoiceMatcher.match(w("up" to 0.60), 5_000L, true))
        assertNull(VoiceMatcher.match(w("am" to 1.0), 5_000L, true))
        assertNull(VoiceMatcher.match(w("i" to 0.92), 5_000L, true))
        assertNull(VoiceMatcher.match(w("am" to 0.9, "up" to 0.3), 5_000L, true))
        assertNull(VoiceMatcher.match(w("up" to 0.9, "am" to 0.9), 5_000L, true))        // wrong order
        assertNull(VoiceMatcher.match(w("alarm" to 0.9, "up" to 0.9), 5_000L, true))     // not a lead word
        assertNull(VoiceMatcher.match(w("am" to 0.9, "up" to 0.6), 1_000L, true))        // too early
    }
}
