package pl.gov.mf.etoll.front.configvehicleselection

import junit.framework.TestCase.assertTrue
import org.junit.Test
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.RecentVehiclesBuffer
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.add

class RecentVehiclesBufferKtTest {

    @Test
    fun recentVehiclesBufferTest() {
        val vehicleId1 = 463324437L
        val vehicleId2 = 563324437L
        val vehicleId3 = 663324437L
        val vehicleId4 = 763324437L

        val buffer = RecentVehiclesBuffer()
        buffer.add(vehicleId1).add(vehicleId2).add(vehicleId3)
        assertTrue(buffer.recentVehiclesIds.size == 3)
        assertTrue(buffer.recentVehiclesIds[0] == vehicleId3)
        assertTrue(buffer.recentVehiclesIds[1] == vehicleId2)
        assertTrue(buffer.recentVehiclesIds[2] == vehicleId1)

        buffer.add(vehicleId4)
        assertTrue(buffer.recentVehiclesIds.size == 3)
        assertTrue(buffer.recentVehiclesIds[0] == vehicleId4)
        assertTrue(buffer.recentVehiclesIds[1] == vehicleId3)
        assertTrue(buffer.recentVehiclesIds[2] == vehicleId2)

        val vehicle5 = 863324437L

        buffer.add(vehicle5)
        assertTrue(buffer.recentVehiclesIds.size == 3)
        assertTrue(buffer.recentVehiclesIds[0] == vehicle5)
        assertTrue(buffer.recentVehiclesIds[1] == vehicleId4)
        assertTrue(buffer.recentVehiclesIds[2] == vehicleId3)

        buffer.add(vehicleId4)
        assertTrue(buffer.recentVehiclesIds.size == 3)
        assertTrue(buffer.recentVehiclesIds[0] == vehicleId4)
        assertTrue(buffer.recentVehiclesIds[1] == vehicle5)
        assertTrue(buffer.recentVehiclesIds[2] == vehicleId3)
    }
}