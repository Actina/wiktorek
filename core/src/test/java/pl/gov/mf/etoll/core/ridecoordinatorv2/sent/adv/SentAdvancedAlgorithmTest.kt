package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.RemoveEmptyLocationsProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.SentGeoAlgorithmProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter.GpsConverter
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter.NkspoConverter
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.datasource.FileDataSource
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.exporter.ConsoleCsvExporter
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.player.RideDataPlayer
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.player.RideDataPlayerImpl

class SentAdvancedAlgorithmTest {

    private lateinit var rideDataPlayer: RideDataPlayer

    @Before
    fun setUp() {
    }

    @Test
    fun checkOutputForServerLog() {
        rideDataPlayer = RideDataPlayerImpl(
            FileDataSource(NkspoConverter()),
            ConsoleCsvExporter(),
            listOf(
                RemoveEmptyLocationsProcessor(),
                SentGeoAlgorithmProcessor()
            )
        )
        rideDataPlayer.start("trasa_note8_serverlog.csv")
        var stop = false
        do {
            stop = !rideDataPlayer.playNextLocation()
        } while (!stop)
        assertEquals(108, rideDataPlayer.getSumOfGeneratedEvents())
    }

    @Test
    fun checkOutputForGps() {
        rideDataPlayer = RideDataPlayerImpl(
            FileDataSource(GpsConverter()),
            ConsoleCsvExporter(),
            listOf(
                RemoveEmptyLocationsProcessor(), /*FlowControlProcessor(3),*/
                SentGeoAlgorithmProcessor()
            )
        )
        rideDataPlayer.start("trasa_note8_gpslog.csv")
        var stop = false
        do {
            stop = !rideDataPlayer.playNextLocation()
        } while (!stop)
        assertEquals(151, rideDataPlayer.getSumOfGeneratedEvents())
    }
}