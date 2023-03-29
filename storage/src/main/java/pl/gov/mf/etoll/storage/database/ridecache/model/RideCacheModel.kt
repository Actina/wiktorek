package pl.gov.mf.etoll.storage.database.ridecache.model

open class RideCacheModel(
    var id: Long = 0,
    var timestamp: Long = 0,
    var typeId: Int,
    var serviceTypeId: Int,
    var payload: String = "{}",
    var withLocation: Boolean,
    var fromPureGps: Boolean,
    var targetSystem: Int
) {
    override fun toString(): String {
        return "$timestamp : $typeId : $serviceTypeId : $payload"
    }
}