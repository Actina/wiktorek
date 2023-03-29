package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv

import pl.gov.mf.etoll.core.model.CoreConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.SentAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.LocationProcessorsList
import pl.gov.mf.mobile.utils.LocationWrapper
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class SentAdvancedAlgorithm @Inject constructor(private val preprocessors: LocationProcessorsList) :
    SentAlgorithm {

    companion object {
        fun updateConfiguration(configuration: CoreConfiguration) {
            // TODO: add code here after backend starts to support it
        }


        /**
         * When we analyze next gps input, then we check if diff between last analyzed and new one
         * is at least this long - if no, then we skip this data
         */
        var minimumTickDiffTimeInMilliseconds = AtomicLong(2500L)

        /**
         * Maximum time between generating events - parking condition
         */
        var maxWaitingTimeBetweenEventsInMinutes = AtomicInteger(5)

        /**
         * Minimum distance in meters that need to be passed for event generation
         * for condition time AND distance
         */
        var minimumDistanceForEventInMeters = AtomicInteger(1000)


        /**
         * Minimum time in seconds that need to be passed for event generation
         * for condition time AND distance
         */
        var minimumTimeForEventsInSeconds = AtomicInteger(60)

        /**
         * Minimum bearing sum change to generate event
         */
        var minimumBearingChangeInDegrees = AtomicInteger(40)

        /**
         * Minimum speed that will be threated as movement - in meters per second.
         * Default value corresponds to 10km/h and is taken from SENT GEO app
         */
        var minimumSpeedForMovementInMperS = 2.8001

        /**
         * Selected bearing difference counting method
         * abs = increasing sum
         * classic = relative difference
         */
        var selectedAbsBearingSumMethod = true
    }

    override fun resetAlgorithm() {
        for (processor in preprocessors.processors) {
            processor.newProbeSet()
        }
    }

    override fun onNextSecond(location: LocationWrapper?): Boolean {
        if (location == null) return false
        var processedLocation: LocationWrapper? = location
        for (processor in preprocessors.processors) {
            if (processedLocation != null)
                processedLocation =
                    if (processor.processData(processedLocation.toAlgModel()) != null) processedLocation else null
        }
        return processedLocation != null
    }


}

private fun LocationWrapper.toAlgModel(): LocationData =
    LocationData(
        0,
        latitude,
        longitude,
        altitude,
        bearing.toDouble(),
        speed.toDouble(),
        accuracy.toDouble(),
        time
    )
