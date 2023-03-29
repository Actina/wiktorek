package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.datasource

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter.DataConverter
import java.io.BufferedReader
import java.io.File

class FileDataSource(private val converter: DataConverter) : DataSource {
    private var file: BufferedReader? = null
    override fun open(set: String) {
        if (file != null)
            close()
        val classLoader = javaClass.classLoader
        classLoader.getResource(set)?.file?.let {
            file = File(it).bufferedReader()
            // read first line with headers
            val ignoredLine = file?.readLine()
            println("Input file header: $ignoredLine")
        }
    }

    override fun close() {
        if (file == null) return
        try {
            file?.close()
        } catch (ex: Exception) {

        }
        file = null
    }

    override fun getNextLocation(): LocationData? {
        return try {
            val input = file?.readLine()
            input?.let { converter.convertInput(it) }
        } catch (ex: Exception) {
            null
        }
    }
}