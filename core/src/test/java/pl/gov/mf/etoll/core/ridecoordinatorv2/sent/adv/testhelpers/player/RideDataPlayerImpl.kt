package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.player

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.LocationDataProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.datasource.DataSource
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.exporter.DataExporter

class RideDataPlayerImpl(
    private val dataSource: DataSource,
    private val dataExporter: DataExporter,
    private val processors: List<LocationDataProcessor>
) :
    RideDataPlayer {

    var countedEvents = 0

    override fun start(trackName: String) {
        countedEvents = 0
        dataSource.open(trackName)
        dataExporter.start(trackName.replace(".csv", "") + "_processed.csv")
    }

    override fun stop() {
        dataSource.close()
        dataExporter.stop()
    }

    override fun playNextLocation(): Boolean {
        val locationInput = dataSource.getNextLocation()
        if (locationInput == null) {
            stop()
            return false
        }
        // process location
        var processedData = locationInput
        for (processor in processors) {
            if (processedData != null)
                processedData = processor.processData(processedData)
        }
        processedData?.let {
            countedEvents++
            dataExporter.onNext(it)
        }
        return true
    }

    override fun getSumOfGeneratedEvents() = countedEvents
}