package pl.gov.mf.etoll.storage.settings

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class SettingsUC {

    class ReadSettingsUseCase(private val ds: SettingsManager) : SettingsUC() {

        fun executeForStringAsync(settings: Settings): Single<String> =
            ds.getStringSettingAsync(settings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        fun executeForIntAsync(settings: Settings): Single<Int> = ds.getIntSettingAsync(settings)
        fun executeForBooleanAsync(settings: Settings): Single<Boolean> = ds.getFlagAsync(settings)
        fun executeForLongAsync(settings: Settings): Single<Long> = ds.getLongSettingAsync(settings)

        fun executeForString(settings: Settings) = ds.getStringSetting(settings)
        fun executeForInt(settings: Settings) = ds.getIntSetting(settings)
        fun executeForBoolean(settings: Settings) = ds.getFlag(settings)
        fun executeForLong(settings: Settings) = ds.getLongSetting(settings)

        suspend fun executeForStringV2(settings: Settings) = ds.getString(settings)
        suspend fun executeForIntV2(settings: Settings) = ds.getInt(settings)
        suspend fun executeForBooleanV2(settings: Settings) = ds.getBoolean(settings)
        suspend fun executeForLongV2(settings: Settings) = ds.getLong(settings)
    }

    class WriteSettingsUseCase(private val ds: SettingsManager) : SettingsUC() {

        fun execute(
            settings: Settings,
            value: String
        ): Completable = ds.setSettings(settings, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        fun execute(
            settings: Settings,
            value: Int
        ): Completable = ds.setSettings(settings, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        fun execute(
            settings: Settings,
            value: Boolean
        ): Completable = ds.setFlag(settings, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        fun execute(
            settings: Settings,
            value: Long
        ): Completable = ds.setSettings(settings, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        suspend fun executeV2(
            settings: Settings,
            value: String
        ) = ds.setString(settings, value)

        suspend fun executeV2(
            settings: Settings,
            value: Int
        ) = ds.setInt(settings, value)

        suspend fun executeV2(
            settings: Settings,
            value: Boolean
        ) = ds.setBoolean(settings, value)

        suspend fun executeV2(
            settings: Settings,
            value: Long
        ) = ds.setLong(settings, value)

    }

}