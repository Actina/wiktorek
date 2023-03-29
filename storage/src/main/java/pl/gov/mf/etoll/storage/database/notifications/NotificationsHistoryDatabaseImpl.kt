package pl.gov.mf.etoll.storage.database.notifications

import android.content.res.Resources
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Sort
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemPayloadModel
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemRealmModel
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemType
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class NotificationsHistoryDatabaseImpl @Inject constructor(
    private val databaseProvider: RealmDatabaseProvider,
    private val getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase
) : NotificationsHistoryDatabase {

    override fun getAllSorted(): Single<List<NotificationHistoryItemModel>> =
        Single.create<List<NotificationHistoryItemModel>> { emitter ->
            val output = mutableListOf<NotificationHistoryItemModel>()
            val dbInstances = databaseProvider.getDatabase()
            dbInstances.executeTransactionAsync({ dbInstance ->
                val realmResult = dbInstance.where(NotificationHistoryItemRealmModel::class.java)
                    .sort("id", Sort.DESCENDING)
                    .findAll()
                realmResult.forEach {
                    output.add(it.toModel(getCurrentLanguageUseCase.execute()))
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
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getById(id: Long): Single<NotificationHistoryItemModel> =
        Single.create<NotificationHistoryItemModel> { emitter ->
            var success: NotificationHistoryItemModel? = null
            val dbInstances = databaseProvider.getDatabase()
            dbInstances.executeTransactionAsync({ dbInstance ->
                val realmResult = dbInstance.where(NotificationHistoryItemRealmModel::class.java)
                    .equalTo("id", id)
                    .findAll()
                success = realmResult.first()?.toModel(getCurrentLanguageUseCase.execute())
            }, {
                Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                dbInstances.close()
                if (success != null)
                    emitter.onSuccess(success!!)
                else
                    emitter.onError(Resources.NotFoundException())
            }, {
                Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                dbInstances.close()
                emitter.onError(it)
            })
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getByApiMessageId(apiMessageId: String): Single<NotificationHistoryItemModel> =
        Single.create<NotificationHistoryItemModel> { emitter ->
             var success: NotificationHistoryItemModel? = null
             val dbInstances = databaseProvider.getDatabase()
             dbInstances.executeTransactionAsync({ dbInstance ->
                 val realmResult = dbInstance.where(NotificationHistoryItemRealmModel::class.java)
                         .equalTo("messageId", apiMessageId)
                         .findAll()
                 success = realmResult.first()?.toModel(getCurrentLanguageUseCase.execute())
             }, {
                 Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                 dbInstances.close()
                 if (success != null)
                     emitter.onSuccess(success!!)
                 else
                     emitter.onError(Resources.NotFoundException())
             }, {
                 Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
                 dbInstances.close()
                 emitter.onError(it)
             })
             // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
         }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun clear(): Completable =
        Completable.create { emitter ->
            var success = false

            databaseProvider.executeTransaction(
                { db ->
                    success = db.where(NotificationHistoryItemRealmModel::class.java)
                        .findAll()
                        .deleteAllFromRealm()
                },
                {
                    if (success)
                        emitter.onComplete()
                    else
                        emitter.onError(IllegalStateException("Exception while trying to clear NotificationHistoryItemRealmModel table"))
                },
                { emitter.onError(it) }
            )
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun remove(ids: Array<Long>): Completable =
        Completable.create { emitter ->
            var success = false

            databaseProvider.executeTransaction(
                { db ->
                    success = db.where(NotificationHistoryItemRealmModel::class.java)
                        .`in`("id", ids)
                        .findAll()
                        .deleteAllFromRealm()
                },
                {
                    if (success)
                        emitter.onComplete()
                    else
                        emitter.onError(IllegalStateException("Exception while trying to clear NotificationHistoryItemRealmModel table"))
                },
                { emitter.onError(it) }
            )
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun remove(id: Long): Single<Boolean> = Single.create<Boolean> { emitter ->
        var success = true
        val dbInstances = databaseProvider.getDatabase()
        dbInstances.executeTransactionAsync({ dbInstance ->
            val realmResult = dbInstance.where(NotificationHistoryItemRealmModel::class.java)
                .equalTo("id", id)
                .findAll()
            success = success && realmResult.deleteAllFromRealm()
            if (success) {
                Log.d("Database_analyze", "Removed object from notification -> $id")
            } else
                Log.d("Database_analyze", "Removal was NOT SUCCEED from notification -> $id")
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onSuccess(success)
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun addNew(
        type: NotificationHistoryItemType,
        titleRes: String,
        contentRes: String,
        iconRes: Int?,
        payloadJson: String?,
        contentExtraValue: String?,
        apiMessageId: String
    ): Single<Long> = Single.create<Long> { emitter ->
        var id = 0L
        val dbInstances = databaseProvider.getDatabase()
        dbInstances.executeTransactionAsync({ dbInstance ->
            // get latest id +1
            val realmResult =
                dbInstance.where(NotificationHistoryItemRealmModel::class.java)
                    .sort("id", Sort.DESCENDING)
                    .limit(1)
                    .findAll()
            if (realmResult.size > 0)
                id = realmResult[0]!!.id!! + 1L
            // now save object with new value
            val newObject = NotificationHistoryItemRealmModel(
                id,
                type.toInt(),
                titleRes,
                contentRes,
                System.currentTimeMillis(),
                iconRes,
                payloadJson,
                contentExtraValue,
                apiMessageId
            )
            dbInstance.insert(newObject)
            Log.d(
                "Database_analyze",
                "Added object with id ${newObject.id} for notifications history"
            )
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onSuccess(id)
        }, {
            Log.d("DATABASE_COUNTER", "ACTIVE: ${dbInstances.numberOfActiveVersions}")
            dbInstances.close()
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
}

private fun NotificationHistoryItemRealmModel.toModel(language: SupportedLanguages): NotificationHistoryItemModel {
    val model = try {
        payloadJson?.toObject<NotificationHistoryItemPayloadModel>()
    } catch (_: Exception) {
        null
    }
    return NotificationHistoryItemModel(
        id,
        NotificationHistoryItemType.fromInt(type),
        if (model == null) this.titleResource else model.headers?.getForLanguage(language) ?: "",
        if (model == null) this.contentResource else model.contents?.getForLanguage(language) ?: "",
        timestamp,
        iconResource,
        model,
        contentExtraValue,
        messageId
    )
}
