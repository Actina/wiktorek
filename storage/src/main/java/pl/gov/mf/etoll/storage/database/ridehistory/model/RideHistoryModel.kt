package pl.gov.mf.etoll.storage.database.ridehistory.model

data class RideHistoryModel(
    var id: Int = 0,
    var timestamp: Long = 0,
    var type: Int = 0,
    var frameType: Int = 0,
    var additionalData: String = ""
)