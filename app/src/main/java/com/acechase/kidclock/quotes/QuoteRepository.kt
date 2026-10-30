package com.acechase.kidclock.quotes

import android.content.Context
import android.util.Log
import com.acechase.kidclock.Prefs
import org.json.JSONObject
import java.util.Calendar

/**
 * Local-only quote library loaded from assets/quotes.json.
 *
 * Selection works in daily batches: the first time quotes are asked for on a given day,
 * [BATCH] of them are chosen from the pool, excluding the most recently shown [HISTORY] quotes so
 * a child does not see repeats for weeks, each from a different theme so the set feels varied
 * (the first one follows the day's rotating theme). The batch and the position within it are
 * persisted, so the quote on screen survives restarts; tapping it advances to the next one.
 */
class QuoteRepository private constructor(private val quotes: List<Quote>) {

    fun all(): List<Quote> = quotes
    fun byId(id: Int): Quote? = quotes.firstOrNull { it.id == id }

    /** Today's batch of quotes, choosing and persisting a fresh one on the first call of the day. */
    fun todaysBatch(context: Context, now: Calendar = Calendar.getInstance()): List<Quote> {
        if (quotes.isEmpty()) return emptyList()
        val prefs = Prefs(context)
        val dayKey = now.get(Calendar.YEAR) * 1000 + now.get(Calendar.DAY_OF_YEAR)
        if (prefs.quoteDayKey == dayKey) {
            val saved = prefs.quoteBatchIds.mapNotNull { byId(it) }
            if (saved.isNotEmpty()) return saved
        }

        val history = prefs.quoteHistory
        val pool = quotes.filter { it.id !in history }.ifEmpty { quotes }.toMutableList()
        val rnd = java.util.Random(dayKey.toLong() * 31L + quotes.size + prefs.quoteHistory.size)
        val picks = mutableListOf<Quote>()
        // One quote per theme, starting from the day's theme so consecutive days lead with different flavours.
        var t = dayKey % THEMES.size
        while (picks.size < BATCH && pool.isNotEmpty()) {
            val themed = pool.filter { THEMES[t % THEMES.size] in it.tags }.ifEmpty { pool }
            val pick = themed[rnd.nextInt(themed.size)]
            picks += pick; pool -= pick
            t++
        }

        prefs.quoteDayKey = dayKey
        prefs.quoteBatchIds = picks.map { it.id }
        prefs.quoteIndex = 0
        prefs.quoteOfDayId = picks.first().id
        prefs.quoteHistory = (picks.map { it.id } + history).take(HISTORY)
        Log.i(TAG, "quotes for day $dayKey: ${picks.joinToString { "#" + it.id }}")
        return picks
    }

    /** The quote currently showing (clock face and gentle wake-up card agree on this). */
    fun current(context: Context): Quote? {
        val batch = todaysBatch(context)
        if (batch.isEmpty()) return null
        return batch[Math.floorMod(Prefs(context).quoteIndex, batch.size)]
    }

    /** Position within today's batch as "n of 6", for the settings preview. */
    fun position(context: Context): String {
        val batch = todaysBatch(context)
        return if (batch.isEmpty()) "" else "${Math.floorMod(Prefs(context).quoteIndex, batch.size) + 1} of ${batch.size}"
    }

    /** Advance to the next quote in today's batch (tap on the quote). Wraps around. */
    fun next(context: Context): Quote? {
        val batch = todaysBatch(context)
        if (batch.isEmpty()) return null
        val prefs = Prefs(context)
        prefs.quoteIndex = Math.floorMod(prefs.quoteIndex + 1, batch.size)
        return current(context)
    }

    /** Kept for callers that want "the" quote of the day: it is whichever one is showing. */
    fun quoteOfTheDay(context: Context): Quote? = current(context)

    /** Throw today's batch away and draw a new one (parent settings "new set"). */
    fun reroll(context: Context): Quote? {
        Prefs(context).quoteDayKey = 0
        return current(context)
    }

    companion object {
        private const val TAG = "Quotes"
        private const val BATCH = 6
        private const val HISTORY = 180 // ~30 days of batches before a quote can come back
        private val THEMES = listOf("courage", "kindness", "curiosity", "perseverance", "self-worth", "dreams", "learning", "humor", "mindfulness", "friendship")

        @Volatile private var instance: QuoteRepository? = null

        fun get(context: Context): QuoteRepository = instance ?: synchronized(this) {
            instance ?: load(context.applicationContext).also { instance = it }
        }

        private fun load(context: Context): QuoteRepository {
            val list = mutableListOf<Quote>()
            runCatching {
                val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
                val arr = JSONObject(json).getJSONArray("quotes")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val tags = o.optJSONArray("tags")?.let { t -> List(t.length()) { t.getString(it) } } ?: emptyList()
                    list += Quote(o.getInt("id"), o.getString("text"), o.optString("author").takeIf { it.isNotEmpty() && it != "null" },
                        o.optString("source").takeIf { it.isNotEmpty() && it != "null" }, tags)
                }
            }.onFailure { Log.e(TAG, "failed to load quotes.json", it) }
            Log.i(TAG, "loaded ${list.size} quotes")
            return QuoteRepository(list)
        }
    }
}
