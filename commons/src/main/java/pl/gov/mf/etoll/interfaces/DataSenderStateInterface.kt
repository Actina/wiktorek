package pl.gov.mf.etoll.interfaces

interface DataSenderStateInterface {
    fun onDataSenderStateChanged(newLevel: WarningsBasicLevels)
}

interface DataSenderStateInterfaceController {
    fun setCallback(dataSenderInterface: DataSenderStateInterface, tag: String)
    fun removeCallback(tag: String)
    fun start()
    fun stop()
}