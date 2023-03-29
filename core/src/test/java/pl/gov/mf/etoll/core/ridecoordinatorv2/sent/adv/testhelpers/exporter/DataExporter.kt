package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.exporter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

interface DataExporter {
    fun start(set: String)
    fun stop()
    fun onNext(data: LocationData)
}