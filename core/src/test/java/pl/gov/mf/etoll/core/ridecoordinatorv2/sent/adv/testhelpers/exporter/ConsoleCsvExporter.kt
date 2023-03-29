package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.exporter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

class ConsoleCsvExporter : DataExporter {
    private lateinit var stringBuilder: StringBuilder
    override fun start(set: String) {
        stringBuilder = StringBuilder("id,lat,lon,alt,speed,timestamp,source\n")
    }

    override fun stop() {
        print(stringBuilder.toString())
    }

    override fun onNext(data: LocationData) {
        stringBuilder =
            stringBuilder.append("${data.id},${data.lat},${data.lon},${data.altitude},${data.speed},${data.timestampInMs},${data.source}\n")
    }
}