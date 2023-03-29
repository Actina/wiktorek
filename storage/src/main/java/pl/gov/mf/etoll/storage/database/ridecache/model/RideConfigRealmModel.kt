package pl.gov.mf.etoll.storage.database.ridecache.model

import io.realm.RealmObject

open class RideConfigRealmModel(
    var lastUsedSpoeKasId: Long = 0,
    var sent: Boolean = false
) : RealmObject()