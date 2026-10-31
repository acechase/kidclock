package com.acechase.kidclock.holiday

import android.content.Context
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.looks.Look

/** The background and the look that make up each holiday's design. */
object HolidayDesigns {
    fun face(context: Context, h: Holiday): ClockFace = when (h) {
        Holiday.NEW_YEARS_EVE -> NewYearFace(context)
        Holiday.VALENTINES -> ValentineFace(context)
        Holiday.ST_PATRICKS -> StPatrickFace(context)
        Holiday.EASTER -> EasterFace(context)
        Holiday.FOURTH_OF_JULY -> FourthJulyFace(context)
        Holiday.HALLOWEEN -> HalloweenFace(context)
        Holiday.THANKSGIVING -> ThanksgivingFace(context)
        Holiday.CHRISTMAS -> ChristmasFace(context)
    }

    fun look(h: Holiday): Look = when (h) {
        Holiday.NEW_YEARS_EVE -> MidnightLook()
        Holiday.VALENTINES -> RoseLook()
        Holiday.ST_PATRICKS -> CloverLook()
        Holiday.EASTER -> PastelEggsLook()
        Holiday.FOURTH_OF_JULY -> FireworksLook()
        Holiday.HALLOWEEN -> WitchingHourLook()
        Holiday.THANKSGIVING -> HarvestLook()
        Holiday.CHRISTMAS -> FairyLightsLook()
    }
}
