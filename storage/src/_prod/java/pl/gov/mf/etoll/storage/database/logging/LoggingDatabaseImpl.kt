package pl.gov.mf.etoll.storage.database.logging

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Sort
import org.joda.time.DateTime
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.storage.database.logging.LoggingDatabase
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel
import pl.gov.mf.etoll.storage.database.logging.model.LoggingRealmModel
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider
import javax.inject.Inject

class LoggingDatabaseImpl @Inject constructor(
    private val databaseProvider: RealmDatabaseProvider
) : LoggingDatabase {

    override fun getNextChunk(): Single<List<LoggingModel>> = Single.just(emptyList())

    override fun removeOldest(count: Int): Completable = Completable.create { emitter ->
        emitter.onComplete()
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun add(item: LoggingModel): Completable = Completable.create { emitter ->
        emitter.onError(IllegalStateException("Should not be called in this flavour"))
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun log(tag: String, description: String) {

    }
}
