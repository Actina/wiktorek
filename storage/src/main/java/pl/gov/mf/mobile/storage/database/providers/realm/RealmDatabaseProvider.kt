package pl.gov.mf.mobile.storage.database.providers.realm

import io.realm.Realm
import pl.gov.mf.etoll.initialization.LoadableSystem

interface RealmDatabaseProvider : LoadableSystem {
    fun getDatabase(): Realm

    fun executeTransaction(
        transaction: Realm.Transaction,
        onSuccess: Realm.Transaction.OnSuccess?,
        onError: Realm.Transaction.OnError?
    )
}