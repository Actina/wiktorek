package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.exporter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

class ConsoleExporter : DataExporter {
    private var counter = 0
    override fun start(set: String) {
        println("Starting exporter for $set")
        counter = 0
    }

    override fun stop() {
        println("Exporter finished count=$counter")
    }

    override fun onNext(data: LocationData) {
        counter++
        println("Data after processing: ${data.toJSON()}")
    }
}