package com.acechase.kidclock.voice

/**
 * Decides whether what the recogniser heard is a command. Pure, so the rules are unit-tested
 * without Vosk: the listener parses the recogniser's JSON into [Word]s and asks [match].
 */
object VoiceMatcher {
    enum class Command { SNOOZE, DISMISS }

    /** One recognised word with the recogniser's confidence in it; "[unk]" is anything outside the grammar. */
    data class Word(val word: String, val conf: Double)

    const val GRAMMAR = """["snooze", "snooze please", "stop", "stop alarm", "alarm off", "off", "turn off", "turn off alarm", "i'm awake", "i am awake", "i'm up", "i am up", "[unk]"]"""

    /** Every phrase that ends an alarm also ends a gentle wake-up, so a kid never has to remember which screen wants which words. */
    val COMMANDS = mapOf(
        "snooze" to Command.SNOOZE, "snooze please" to Command.SNOOZE,
        "stop" to Command.DISMISS, "stop alarm" to Command.DISMISS, "alarm off" to Command.DISMISS, "off" to Command.DISMISS,
        "turn off" to Command.DISMISS, "turn off alarm" to Command.DISMISS,
        "i'm awake" to Command.DISMISS, "i am awake" to Command.DISMISS, "i'm up" to Command.DISMISS, "i am up" to Command.DISMISS,
    )

    const val MIN_CONF = 0.70
    /** In the wake window the mic is open for many minutes, so a one-word "stop" or "off" needs to be heard very clearly. */
    const val SINGLE_WORD_CONF = 0.92
    /** The "I'm up" family is matched word by word at this confidence; see [match]. */
    const val UP_FAMILY_CONF = 0.45
    const val WARMUP_MS = 1500L
    const val UNK = "[unk]"

    private val upFamilyEnd = setOf("up", "awake")
    private val upFamilyLead = setOf("i", "i'm", "am")

    /**
     * The command in [words], or null. [sinceStartMs] is how long the mic has been open; the first
     * moment is skipped because opening the mic produces junk. In [wakeWindowMode] snooze means
     * nothing and a single word must be very clear.
     *
     * Two rules. A whole grammar phrase with every word clear is a command. And the "I'm up" family
     * (what the button says, so what a kid says, fast): the recogniser hears that as "am up", "i'm up"
     * or "i am up" with the short "up" at middling confidence, so a run of lead words ending in "up"
     * or "awake", at least two words, every one at [UP_FAMILY_CONF], also counts. Anything outside
     * the grammar ("[unk]") may come before or after that run but not inside it: "I am not up" is
     * heard as "am [unk] up" and stays a no.
     */
    fun match(words: List<Word>, sinceStartMs: Long, wakeWindowMode: Boolean): Command? {
        if (sinceStartMs < WARMUP_MS || words.isEmpty()) return null
        val known = words.filter { it.word != UNK }
        if (known.isEmpty()) return null
        val hasUnk = known.size != words.size
        val minConf = if (hasUnk) 0.0 else known.minOf { it.conf }
        val text = known.joinToString(" ") { it.word }
        val cmd = COMMANDS[text]
        if (cmd != null && minConf >= MIN_CONF) {
            if (wakeWindowMode && (cmd == Command.SNOOZE || (known.size < 2 && minConf < SINGLE_WORD_CONF))) return null
            return cmd
        }
        return if (isUpFamily(words)) Command.DISMISS else null
    }

    private fun isUpFamily(words: List<Word>): Boolean {
        val run = words.dropWhile { it.word == UNK }.dropLastWhile { it.word == UNK }
        if (run.size < 2 || run.any { it.word == UNK }) return false
        if (run.last().word !in upFamilyEnd) return false
        if (!run.dropLast(1).all { it.word in upFamilyLead }) return false
        return run.all { it.conf >= UP_FAMILY_CONF }
    }
}
