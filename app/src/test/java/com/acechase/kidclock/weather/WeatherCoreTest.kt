package com.acechase.kidclock.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

/**
 * The weather feature must fail soft: no internet, a dead service, or garbage responses may only ever mean
 * "no weather shown". These tests drive [WeatherCore] with a fake network and a fake clock.
 */
class WeatherCoreTest {
    private class FakeStore(
        override var enabled: Boolean = true,
        override var fahrenheit: Boolean = true,
        override var lat: Double = 45.5,
        override var lon: Double = -122.6,
        override var place: String = "Portland",
        override var weatherJson: String? = null,
    ) : WeatherCore.Store

    /** Scripted HTTP: each call looks up its URL prefix; anything unscripted throws like an outage. */
    private class FakeNet {
        val calls = mutableListOf<String>()
        var handler: (String) -> String = { throw UnknownHostException("no network") }
        fun http(url: String): String { calls += url; return handler(url) }
    }

    private var nowMs = 1_700_000_000_000L
    private fun core(net: FakeNet) = WeatherCore(http = net::http, now = { nowMs })

    private fun forecastJson(temp: Double = 55.0) = """
        {"current":{"temperature_2m":$temp,"weather_code":3},
         "hourly":{"time":["2023-11-14T22:00","2023-11-14T23:00","2023-11-15T00:00"],
                   "temperature_2m":[55,54,53],"weather_code":[3,3,61],"precipitation_probability":[10,20,60]},
         "daily":{"time":["2023-11-14","2023-11-15"],"temperature_2m_max":[58,52],"temperature_2m_min":[44,40],
                  "weather_code":[3,61],"precipitation_probability_max":[20,80]}}
    """.trimIndent()

    private fun goodNet() = FakeNet().apply {
        handler = { url ->
            when {
                url.startsWith("https://api.open-meteo.com/") -> forecastJson()
                url == WeatherCore.IP_URL -> """{"loc":"40.7,-74.0","city":"New York"}"""
                else -> throw IOException("unscripted $url")
            }
        }
    }

    // ---- happy path, so the failure tests are meaningful ----

    @Test
    fun successfulRefreshFillsTheCache() {
        val net = goodNet(); val store = FakeStore(); val c = core(net)
        val r = c.refresh(store)
        assertTrue(r.isSuccess)
        val w = c.usable(store)!!
        assertEquals(55.0, w.temp, 0.01); assertEquals(58.0, w.high, 0.01); assertEquals(44.0, w.low, 0.01)
        assertEquals(2, w.daily.size); assertEquals("Portland", w.place); assertTrue(w.fahrenheit)
        assertEquals(1, net.calls.size)   // location was set, so no IP lookup
        assertTrue(net.calls[0].contains("temperature_unit=fahrenheit"))
    }

    // ---- outages ----

    @Test
    fun offlineRefreshFailsSoftlyAndLeavesCacheUntouched() {
        val net = FakeNet()   // every call throws UnknownHostException
        val store = FakeStore(weatherJson = cachedJson(ageMs = 45 * 60_000L))
        val before = store.weatherJson
        val r = core(net).refresh(store)
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is UnknownHostException)
        assertEquals(before, store.weatherJson)
    }

    @Test
    fun serviceGoneHttpErrorFailsSoftly() {
        val net = FakeNet().apply { handler = { throw IllegalStateException("HTTP 503 for $it") } }
        val store = FakeStore(weatherJson = null)
        val r = core(net).refresh(store)
        assertTrue(r.isFailure)
        assertNull(store.weatherJson)
    }

    @Test
    fun malformedBodyIsRejectedWholeAndCacheKept() {
        val cached = cachedJson(ageMs = 40 * 60_000L)
        for (body in listOf("<html>maintenance</html>", "", "{}", """{"current":{"temperature_2m":50}}""",
                            """{"current":{"temperature_2m":50,"weather_code":1},"hourly":{"time":[],"temperature_2m":[],"weather_code":[]},"daily":{"time":[],"temperature_2m_max":[],"temperature_2m_min":[],"weather_code":[]}}""")) {
            val net = FakeNet().apply { handler = { body } }
            val store = FakeStore(weatherJson = cached)
            val r = core(net).refresh(store)
            assertTrue("body '$body' should fail", r.isFailure)
            assertEquals(cached, store.weatherJson)
        }
    }

    @Test
    fun ipLocationServiceDownWithNoLocationSetMeansNoForecastAndNoForecastCall() {
        val net = goodNet().apply {
            val inner = handler
            handler = { url -> if (url == WeatherCore.IP_URL) throw IOException("ipinfo gone") else inner(url) }
        }
        val store = FakeStore(lat = Double.NaN, lon = Double.NaN, place = "")
        val r = core(net).refresh(store)
        assertTrue(r.isFailure)
        assertNull(store.weatherJson)
        assertEquals(listOf(WeatherCore.IP_URL), net.calls)
    }

    @Test
    fun withoutASavedLocationTheIpGuessIsUsed() {
        val net = goodNet()
        val store = FakeStore(lat = Double.NaN, lon = Double.NaN, place = "")
        assertTrue(core(net).refresh(store).isSuccess)
        assertEquals(2, net.calls.size)
        assertTrue(net.calls[1].contains("latitude=40.7&longitude=-74.0"))
        assertEquals("New York", Weather.fromJson(store.weatherJson)!!.place)
    }

    // ---- refresh policy and backoff ----

    @Test
    fun freshCacheNeedsNoRefreshAndOldCacheDoes() {
        val c = core(FakeNet())
        assertFalse(c.needsRefresh(FakeStore(weatherJson = cachedJson(ageMs = 10 * 60_000L))))
        assertTrue(c.needsRefresh(FakeStore(weatherJson = cachedJson(ageMs = 31 * 60_000L))))
        assertTrue(c.needsRefresh(FakeStore(weatherJson = null)))
    }

    @Test
    fun unitChangeForcesARefresh() {
        val c = core(FakeNet())
        assertTrue(c.needsRefresh(FakeStore(fahrenheit = false, weatherJson = cachedJson(ageMs = 1_000L, fahrenheit = true))))
    }

    @Test
    fun weatherOffNeverRefreshes() {
        assertFalse(core(FakeNet()).needsRefresh(FakeStore(enabled = false, weatherJson = null)))
    }

    @Test
    fun afterAFailureItBacksOffForFiveMinutesThenTriesAgain() {
        val net = FakeNet(); val c = core(net); val store = FakeStore(weatherJson = null)
        assertTrue(c.needsRefresh(store))
        assertTrue(c.refresh(store).isFailure)
        assertFalse("just failed: must not hammer a dead service", c.needsRefresh(store))
        nowMs += WeatherCore.RETRY_MS - 1
        assertFalse(c.needsRefresh(store))
        nowMs += 2
        assertTrue(c.needsRefresh(store))
        // keeps failing, keeps backing off; cache stays empty rather than half-written
        assertTrue(c.refresh(store).isFailure)
        assertNull(store.weatherJson)
        assertEquals(2, net.calls.size)
    }

    // ---- staleness: never show an old forecast as if it were current ----

    @Test
    fun cachedForecastIsShownUntilThreeHoursOldThenHidden() {
        val c = core(FakeNet())
        assertNotNull(c.usable(FakeStore(weatherJson = cachedJson(ageMs = WeatherCore.STALE_MS - 60_000L))))
        val stale = FakeStore(weatherJson = cachedJson(ageMs = WeatherCore.STALE_MS + 60_000L))
        assertNull("stale forecast must be hidden from the clock face", c.usable(stale))
        assertNotNull("but still known, so the detail screen can say when it was last updated", c.cachedAny(stale))
        assertTrue(c.isStale(c.cachedAny(stale)!!))
    }

    @Test
    fun longOutageHidesWeatherButLaterRecoveryRestoresIt() {
        val net = FakeNet(); val c = core(net)
        val store = FakeStore(weatherJson = cachedJson(ageMs = 0))
        // Internet drops for a day; refresh attempts fail every retry window; cache survives then goes stale.
        repeat(24 * 12) { nowMs += WeatherCore.RETRY_MS; if (c.needsRefresh(store)) assertTrue(c.refresh(store).isFailure) }
        assertNull(c.usable(store))
        assertNotNull(c.cachedAny(store))
        // Internet returns.
        net.handler = goodNet().handler
        nowMs += WeatherCore.RETRY_MS
        assertTrue(c.needsRefresh(store))
        assertTrue(c.refresh(store).isSuccess)
        assertNotNull(c.usable(store))
    }

    // ---- model robustness ----

    @Test
    fun corruptCacheReadsAsNothing() {
        assertNull(Weather.fromJson(null))
        assertNull(Weather.fromJson(""))
        assertNull(Weather.fromJson("not json"))
        assertNull(Weather.fromJson("""{"temp":"warm"}"""))
    }

    @Test
    fun geocodeReturnsNullForNoMatchAndThrowsOffline() {
        val none = WeatherCore(http = { """{"generationtime_ms":0.1}""" })
        assertNull(none.geocode("Nowhereville"))
        val off = WeatherCore(http = { throw UnknownHostException("offline") })
        assertTrue(runCatching { off.geocode("Portland") }.isFailure)
    }

    private fun cachedJson(ageMs: Long, fahrenheit: Boolean = true): String =
        WeatherCore.parseForecast(forecastJson(), "Portland", fahrenheit, nowMs - ageMs).toJson()
}
