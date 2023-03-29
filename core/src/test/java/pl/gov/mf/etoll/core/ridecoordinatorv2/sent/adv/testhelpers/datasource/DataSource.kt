package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.datasource

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

interface DataSource {
    fun open(set: String)
    fun close()
    fun getNextLocation(): LocationData?
}