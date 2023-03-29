package pl.gov.mf.etoll.storage.settings

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.initialization.LoadableSystem

interface SettingsManager : LoadableSystem {

    fun getFlagAsync(setting: Settings): Single<Boolean>
    fun getIntSettingAsync(setting: Settings): Single<Int>
    fun getStringSettingAsync(setting: Settings): Single<String>
    fun getLongSettingAsync(setting: Settings): Single<Long>

    fun setFlag(
        setting: Settings,
        value: Boolean
    ): Completable

    fun setSettings(
        setting: Settings,
        value: Int
    ): Completable

    fun setSettings(
        setting: Settings,
        value: String
    ): Completable

    fun setSettings(
        setting: Settings,
        value: Long
    ): Completable

    fun getIntSetting(setting: Settings): Int
    fun getFlag(setting: Settings): Boolean
    fun getStringSetting(setting: Settings): String
    fun getLongSetting(setting: Settings): Long

    suspend fun getInt(setting: Settings): Int
    suspend fun getBoolean(setting: Settings): Boolean
    suspend fun getString(setting: Settings): String
    suspend fun getLong(setting: Settings): Long

    suspend fun setInt(setting: Settings, value: Int)
    suspend fun setBoolean(setting: Settings, value: Boolean)
    suspend fun setString(setting: Settings, value: String)
    suspend fun setLong(setting: Settings, value: Long)

}