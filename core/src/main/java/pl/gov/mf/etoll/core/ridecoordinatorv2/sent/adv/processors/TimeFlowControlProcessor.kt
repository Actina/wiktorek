package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.Seconds
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.SentAdvancedAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import javax.inject.Inject

class TimeFlowControlProcessor @Inject constructor() : LocationDataProcessor {

    companion object {
        val minOffset: Long
            get() = SentAdvancedAlgorithm.minimumTickDiffTimeInMilliseconds.get() // milis

        val maxWaitingTime
            get() = SentAdvancedAlgorithm.maxWaitingTimeBetweenEventsInMinutes.get() * 60 // seconds
    }

    private var lastSaved = 0L
    private var timeCounter = 0L
    override fun newProbeSet() {
        lastSaved = 0
    }

    override fun processData(input: LocationData): LocationData? {
        val analyzedDateNow = System.currentTimeMillis()
        val secondsBetweenEvents =
            Seconds.secondsBetween(DateTime(timeCounter), DateTime(analyzedDateNow)).seconds
        Log.d(
            "TIMING",
            "SECONDS: $secondsBetweenEvents diff: ${input.timestampInMs - lastSaved}"
        )
        // check if passed 5 mins since last correct location saving
        if (secondsBetweenEvents >= maxWaitingTime ||
            // check if passed 2.5s since last location time
            input.timestampInMs - lastSaved >= minOffset
        ) {
            lastSaved = input.timestampInMs
            timeCounter = analyzedDateNow
            return input
        }

        return null
    }
}