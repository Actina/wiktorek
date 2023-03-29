package pl.gov.mf.mobile.storage.database.providers.realm

import android.content.Context
import android.util.Log
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class RealmDatabaseProviderImpl @Inject constructor(
    private val context: Context,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
) : RealmDatabaseProvider {

    companion object {
        private const val DATABASE_NAME = "data.realm"
    }

    private lateinit var owner: LoadableSystemsLoader
    private lateinit var config: RealmConfiguration
    private var dbHandler: Realm? = null

    override fun load(): Single<Boolean> = Single.create { emitter ->
        if (::config.isInitialized) {
            emitter.onSuccess(true)
            return@create
        }
        val key =
            readSettingsUseCase.executeForString(Settings.REALM_KEY).toByteArray(Charsets.UTF_8)
        Realm.init(context)
        config = RealmConfiguration.Builder()
            .name(DATABASE_NAME)
            .deleteRealmIfMigrationNeeded() // until we establish the model
            .encryptionKey(key)
            .build()
        dbHandler = getDatabase()
        Log.d("DATABASE_COUNTER", "ACTIVE: ${dbHandler?.numberOfActiveVersions}")
        emitter.onSuccess(true)
    }

    override fun getDatabase(): Realm {
        if (!::config.isInitialized) {
            owner.loadSequentially()
        }
        val instance = Realm.getInstance(config)
        while (instance.isInTransaction)
            Thread.sleep(10)
        return instance
    }

    override fun executeTransaction(
        transaction: Realm.Transaction,
        onSuccess: Realm.Transaction.OnSuccess?,
        onError: Realm.Transaction.OnError?
    ) {
        getDatabase().run {
            executeTransactionAsync(transaction, {
                Log.d("DATABASE_COUNTER", "ACTIVE: $numberOfActiveVersions")
                close()
                onSuccess?.onSuccess()
            }, {
                Log.d("DATABASE_COUNTER", "ACTIVE: $numberOfActiveVersions")
                close()
                onError?.onError(it)
            })
        }
    }

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        owner = loadableSystemsLoader
    }
}