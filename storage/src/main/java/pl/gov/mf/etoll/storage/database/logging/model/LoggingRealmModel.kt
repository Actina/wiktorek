package pl.gov.mf.etoll.storage.database.logging.model

import io.realm.RealmObject

open class LoggingRealmModel(
    var id: Long? = -1,
    var date: String = "",
    var tag: String = "",
    var description: String = ""
) : RealmObject()