package pl.gov.mf.etoll.storage.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.security.SecurityUC.GetMasterKeyUseCase
import pl.gov.mf.etoll.storage.settings.SettingsType.*
import pl.gov.mf.etoll.storage.settings.defaults.SettingsDefaultsProvider
import java.util.*
import javax.inject.Inject

class SettingsManagerSecureImpl @Inject constructor(
    private val masterKeyUseCase: GetMasterKeyUseCase,
    private val context: Context,
    private val settingsDefaultsProvider: SettingsDefaultsProvider,
) : SettingsManager {

    companion object EncryptedSharedPreferencesCO {
        private const val FILE_NAME = "secret_shared_prefs"
        private const val KEY_ALIAS = "key_for_shared_pref"
    }

    private lateinit var owner: LoadableSystemsLoader
    private val booleanCache: EnumMap<Settings, Boolean> = EnumMap(Settings::class.java)
    private val stringCache: EnumMap<Settings, String> = EnumMap(Settings::class.java)
    private val intCache: EnumMap<Settings, Int> = EnumMap(Settings::class.java)
    private val longCache: EnumMap<Settings, Long> = EnumMap(Settings::class.java)

    private lateinit var sharedPreferences: SharedPreferences

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        owner = loadableSystemsLoader
    }

    private fun checkAndSetDefaults(): Completable = Completable.create {
        Settings.values()
            .forEach { setting ->
                if (!sharedPreferences.contains(setting.name)) {
                    when (setting.type) {
                        BOOLEAN -> {
                            booleanCache[setting] =
                                settingsDefaultsProvider.getDefaultValueFor(setting) as Boolean
                            sharedPreferences.edit()
                                .putBoolean(setting.name, booleanCache[setting]!!)
                                .apply()
                        }
                        INT -> {
                            intCache[setting] =
                                settingsDefaultsProvider.getDefaultValueFor(setting) as Int
                            sharedPreferences.edit()
                                .putInt(setting.name, intCache[setting]!!)
                                .apply()
                        }
                        STRING -> {
                            stringCache[setting] =
                                settingsDefaultsProvider.getDefaultValueFor(setting) as String
                            sharedPreferences.edit()
                                .putString(setting.name, stringCache[setting])
                                .apply()
                        }
                        LONG -> {
                            longCache[setting] =
                                settingsDefaultsProvider.getDefaultValueFor(setting) as Long
                            sharedPreferences.edit()
                                .putLong(setting.name, longCache[setting]!!)
                                .apply()
                        }
                    }
                } else {
                    when (setting.type) {
                        BOOLEAN -> booleanCache[setting] =
                            sharedPreferences.getBoolean(
                                setting.name,
                                BOOLEAN.defaultVal as Boolean
                            )
                        INT -> intCache[setting] =
                            sharedPreferences.getInt(setting.name, INT.defaultVal as Int)
                        STRING -> stringCache[setting] =
                            sharedPreferences.getString(setting.name, STRING.defaultVal as String)
                        LONG -> longCache[setting] =
                            sharedPreferences.getLong(setting.name, LONG.defaultVal as Long)
                    }
                }
            }
        it.onComplete()
    }

    override fun getFlagAsync(setting: Settings): Single<Boolean> = Single.create { emitter ->
        validateLoadState()
        if (setting.type != BOOLEAN) emitter.onError(
            IllegalArgumentException("${setting.name} is not Boolean")
        )
        emitter.onSuccess(
            booleanCache[setting] ?: error(
                IllegalStateException("Settings was not initialized properly - ${setting.name}")
            )
        )
    }

    override fun getIntSettingAsync(setting: Settings): Single<Int> = Single.create { emitter ->
        validateLoadState()
        if (setting.type != INT) emitter.onError(
            IllegalArgumentException("${setting.name} is not Int")
        )
        emitter.onSuccess(
            intCache[setting] ?: error(
                IllegalStateException("Settings was not initialized properly - ${setting.name}")
            )
        )
    }

    override fun getStringSettingAsync(setting: Settings): Single<String> =
        Single.create { emitter ->
            validateLoadState()
            if (setting.type != STRING) emitter.onError(
                IllegalArgumentException("${setting.name} is not String")
            )
            emitter.onSuccess(
                stringCache[setting] ?: error(
                    IllegalStateException("Settings was not initialized properly - ${setting.name}")
                )
            )
        }

    override fun getLongSettingAsync(setting: Settings): Single<Long> =
        Single.create { emitter ->
            validateLoadState()
            if (setting.type != LONG) emitter.onError(
                IllegalArgumentException("${setting.name} is not Long")
            )
            emitter.onSuccess(
                longCache[setting] ?: error(
                    IllegalStateException("Settings was not initialized properly - ${setting.name}")
                )
            )
        }

    override fun getFlag(setting: Settings): Boolean {
        validateLoadState()
        if (setting.type != BOOLEAN) throw
        IllegalArgumentException("${setting.name} is not Boolean")
        return booleanCache[setting]!!
    }

    override fun getIntSetting(setting: Settings): Int {
        validateLoadState()
        if (setting.type != INT)
            throw IllegalArgumentException("${setting.name} is not Int")

        return intCache[setting]!!
    }

    override fun getStringSetting(setting: Settings): String {
        validateLoadState()
        if (setting.type != STRING) throw IllegalArgumentException("${setting.name} is not String")
        return stringCache[setting]!!
    }

    override fun getLongSetting(setting: Settings): Long {
        validateLoadState()
        if (setting.type != LONG) throw IllegalArgumentException("${setting.name} is not Long")
        return longCache[setting]!!
    }

    override suspend fun getInt(setting: Settings): Int {
        validateLoadState()
        if (setting.type != INT) throw IllegalArgumentException("${setting.name} is not Int")
        return intCache[setting]!!
    }

    override suspend fun getBoolean(setting: Settings): Boolean {
        validateLoadState()
        if (setting.type != BOOLEAN) throw IllegalArgumentException("${setting.name} is not Boolean")
        return booleanCache[setting]!!
    }

    override suspend fun getString(setting: Settings): String {
        validateLoadState()
        if (setting.type != STRING) throw IllegalArgumentException("${setting.name} is not String")
        return stringCache[setting]!!
    }

    override suspend fun getLong(setting: Settings): Long {
        validateLoadState()
        if (setting.type != LONG) throw IllegalArgumentException("${setting.name} is not Long")
        return longCache[setting]!!
    }

    override suspend fun setInt(setting: Settings, value: Int) {
        validateLoadState()
        if (setting.type != INT) throw
        IllegalArgumentException("${setting.name} is not Int")
        sharedPreferences.edit().putInt(setting.name, value).apply()
        intCache[setting] = value
    }

    override suspend fun setBoolean(setting: Settings, value: Boolean) {
        validateLoadState()
        if (setting.type != BOOLEAN) throw
        IllegalArgumentException("${setting.name} is not Bool")
        sharedPreferences.edit().putBoolean(setting.name, value).apply()
        booleanCache[setting] = value
    }

    override suspend fun setString(setting: Settings, value: String) {
        validateLoadState()
        if (setting.type != STRING) throw
        IllegalArgumentException("${setting.name} is not String")
        sharedPreferences.edit().putString(setting.name, value).apply()
        stringCache[setting] = value
    }

    override suspend fun setLong(setting: Settings, value: Long) {
        validateLoadState()
        if (setting.type != LONG) throw
        IllegalArgumentException("${setting.name} is not Long")
        sharedPreferences.edit().putLong(setting.name, value).apply()
        longCache[setting] = value
    }

    override fun setFlag(
        setting: Settings,
        value: Boolean
    ): Completable =
        Completable.fromRunnable {
            validateLoadState()
            if (setting.type != BOOLEAN) throw IllegalArgumentException("${setting.name} is not Boolean")
            booleanCache[setting] = value
            sharedPreferences.edit()
                .putBoolean(setting.name, value)
                .apply()
        }

    override fun setSettings(
        setting: Settings,
        value: Int
    ): Completable =
        Completable.fromRunnable {
            validateLoadState()
            if (setting.type != INT) throw IllegalArgumentException("${setting.name} is not Int")
            intCache[setting] = value
            sharedPreferences.edit()
                .putInt(setting.name, value)
                .apply()
        }

    override fun setSettings(
        setting: Settings,
        value: String
    ): Completable =
        Completable.fromRunnable {
            Log.d("SETTINGS_WRITE", "WRITING $setting as $value")
            validateLoadState()
            if (setting.type != STRING) throw IllegalArgumentException("${setting.name} is not String")
            stringCache[setting] = value
            sharedPreferences.edit()
                .putString(setting.name, value)
                .apply()
        }

    override fun setSettings(setting: Settings, value: Long): Completable =
        Completable.fromRunnable {
            Log.d("SETTINGS_WRITE", "WRITING $setting as $value")
            validateLoadState()
            if (setting.type != LONG) throw IllegalArgumentException("${setting.name} is not Long")
            longCache[setting] = value
            sharedPreferences.edit()
                .putLong(setting.name, value)
                .apply()
        }

    override fun load(): Single<Boolean> = Single.create { emitter ->
        if (::sharedPreferences.isInitialized) {
            emitter.onSuccess(true)
            return@create
        }
        val key = masterKeyUseCase.executeSynch(KEY_ALIAS)
        sharedPreferences = EncryptedSharedPreferences.create(
            context, FILE_NAME, key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        checkAndSetDefaults().blockingAwait()
        emitter.onSuccess(true)
    }

    private fun getSharedPreferences(): Single<SharedPreferences> =
        masterKeyUseCase.execute(KEY_ALIAS)
            .map {
                EncryptedSharedPreferences.create(
                    context, FILE_NAME, it,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }

    private fun validateLoadState() {
        if (!::sharedPreferences.isInitialized)
            owner.loadSequentially()
    }
}
