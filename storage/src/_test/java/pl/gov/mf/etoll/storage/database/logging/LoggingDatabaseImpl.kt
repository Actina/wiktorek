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

    override fun getNextChunk(): Single<List<LoggingModel>> =
        Single.create<List<LoggingModel>> { emitter ->
            val output = mutableListOf<LoggingModel>()
            val dbInstances = databaseProvider.getDatabase()
            dbInstances.executeTransactionAsync({ dbInstance ->
                val realmResult =
                    dbInstance.where(LoggingRealmModel::class.java)
                        .sort("id", Sort.ASCENDING)
                        .limit(LoggingDatabase.CHUNK_SIZE)
                        .findAll()
                realmResult.forEach {
                    output.add(it.toModel())
                }
            }, {
                Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                dbInstances.close()
                emitter.onSuccess(output)
            }, {
                Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                dbInstances.close()
                emitter.onError(it)
            })
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun removeOldest(count: Int): Completable = Completable.create { emitter ->
        val dbInstances = databaseProvider.getDatabase()
        var success = false
        dbInstances.executeTransactionAsync({ dbInstance ->
            success = dbInstance.where(LoggingRealmModel::class.java)
                .sort("id", Sort.ASCENDING)
                .limit(count.toLong())
                .findAll().deleteAllFromRealm()
            if (success) {
                Log.d("Database_analyze", "Removed objects(logs) -> amount: $count")
            } else
                Log.d("Database_analyze", "Removal was NOT SUCCEED -> amount: $count")
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onComplete()
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onError(it)
        })
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun add(item: LoggingModel): Completable = Completable.create { emitter ->
        val dbInstances = databaseProvider.getDatabase()
        dbInstances.executeTransactionAsync({ dbInstance ->
            var minId = 0L
            // get last in ID:
            val realmResult =
                dbInstance.where(LoggingRealmModel::class.java)
                    .sort("id", Sort.DESCENDING)
                    .limit(1)
                    .findAll()
            if (realmResult.size > 0)
                minId = (realmResult[0]?.id ?: 0) + 1
            val dbInsertionObject = LoggingRealmModel(minId, item.date, item.tag, item.description)
            dbInstance.insert(dbInsertionObject)
            Log.d(
                "Database_analyze",
                "Added object id=${dbInsertionObject.id} date=${dbInsertionObject.date} tag=${dbInsertionObject.tag} desc=${dbInsertionObject.description}"
            )
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onComplete()
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onError(it)
        })
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun log(tag: String, description: String) {
        // we do not manage this observable
        Log.d("LOGGER_CACHE", "[$tag] ADDING LOG TO CACHE! $description")
        add(
            LoggingModel(
                date = DateTime.now()
                    .toString(TimeUtils.DefaultDateTimeFormatterForBillingAccount),
                tag = tag,
                description = description
            )
        ).subscribe()
    }
}

private fun LoggingRealmModel.toModel(): LoggingModel = LoggingModel(id, date, tag, description)
