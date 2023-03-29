package pl.gov.mf.mobile.utils

import android.location.Location
import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationWrapperTest {

    @Test
    fun receivedLocationTimeEarlierThanLowThresh_normalize_dateChangedTimeLeftAsBefore() {

        val currentDateTime = DateTime.parse("2022-04-11T11:18:03")
        val receivedLocationDateTime = DateTime.parse("2022-04-07T12:18:03")
        val expectedDateTimeAfterNormalization = DateTime("2022-04-11T12:18:03")

        val receivedLocationWrapper = mockLocationWrapper(receivedLocationDateTime)

        assertEquals(expectedDateTimeAfterNormalization.millis,
            receivedLocationWrapper.normalize(currentDateTime).time)
    }

    @Test
    fun receivedLocationTimeNotTooEarlyNotTooLate_normalize_timeNotChanged() {

        val currentDateTime = DateTime.parse("2022-04-11T7:28:12")
        val receivedLocationDateTime = DateTime.parse("2022-04-10T12:18:04")
        val expectedDateTimeAfterNormalization = DateTime("2022-04-10T12:18:04")

        val receivedLocationWrapper = mockLocationWrapper(receivedLocationDateTime)

        assertEquals(expectedDateTimeAfterNormalization.millis,
            receivedLocationWrapper.normalize(currentDateTime).time)
    }

    @Test
    fun receivedLocationTimeLaterThanUpperThresh_normalize_dateChangedTimeLeftAsBefore() {

        val currentDateTime = DateTime.parse("2022-05-15T15:24:03")
        val receivedLocationDateTime = DateTime.parse("2022-05-16T15:24:04")
        val expectedDateTimeAfterNormalization = DateTime("2022-05-15T15:24:04")

        val receivedLocationWrapper = mockLocationWrapper(receivedLocationDateTime)

        assertEquals(expectedDateTimeAfterNormalization.millis,
            receivedLocationWrapper.normalize(currentDateTime).time)
    }

    private fun mockLocationWrapper(receivedLocationDateTime: DateTime): LocationWrapper {
        val mockedReceivedLocation = mockk<Location>()
        every { mockedReceivedLocation.time } returns receivedLocationDateTime.millis
        return LocationWrapper(mockedReceivedLocation)
    }
}
