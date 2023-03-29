package pl.gov.mf.etoll.storage.database.ridecache.model

import io.realm.RealmObject

open class RideCacheRealmModel(
    var id: Long = 0,
    var timestamp: Long = 0,
    var typeId: Int = 0,
    var serviceTypeId: Int = 0,
    var payload: String = "{}",
    var withLocation: Boolean = false,
    var pureGps: Boolean = false,
    var targetSystem: Int = 0
) : RealmObject() {
    fun toModel(): RideCacheModel =
        RideCacheModel(
            id,
            timestamp,
            typeId,
            serviceTypeId,
            payload,
            withLocation,
            pureGps,
            targetSystem
        )

    override fun toString(): String {
        return "$id -> $timestamp : $typeId : $serviceTypeId : $targetSystem : $payload"
    }
}