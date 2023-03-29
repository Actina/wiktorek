package pl.gov.mf.etoll.storage

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.etoll.logging.LoggingHelper
import pl.gov.mf.etoll.security.SecurityUC.GetMasterKeyUseCase
import pl.gov.mf.etoll.storage.database.logging.LoggingDatabase
import pl.gov.mf.etoll.storage.database.logging.LoggingDatabaseImpl
import pl.gov.mf.etoll.storage.database.notifications.NotificationsHistoryDatabase
import pl.gov.mf.etoll.storage.database.notifications.NotificationsHistoryDatabaseImpl
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabaseImpl
import pl.gov.mf.etoll.storage.database.ridehistory.RideHistoryDatabaseManager
import pl.gov.mf.etoll.storage.database.ridehistory.RideHistoryDatabaseManagerImpl
import pl.gov.mf.etoll.storage.settings.SettingsManager
import pl.gov.mf.etoll.storage.settings.SettingsManagerSecureImpl
import pl.gov.mf.etoll.storage.settings.SettingsUC.ReadSettingsUseCase
import pl.gov.mf.etoll.storage.settings.SettingsUC.WriteSettingsUseCase
import pl.gov.mf.etoll.storage.settings.defaults.SettingsDefaultsProvider
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProviderImpl

@Module
class StorageModule {

    @ApplicationScope
    @Provides
    fun providesSettingsManagerSecureImpl(
        masterKeyUseCase: GetMasterKeyUseCase,
        context: Context,
        settingsDefaultsProvider: SettingsDefaultsProvider
    ): SettingsManager =
        SettingsManagerSecureImpl(masterKeyUseCase, context, settingsDefaultsProvider)

    @ApplicationScope
    @Provides
    fun providesDatabaseProvider(impl: RealmDatabaseProviderImpl): RealmDatabaseProvider = impl

    @ApplicationScope
    @Provides
    fun providesRideCacheDatabaseManager(impl: RideCacheDatabaseImpl): RideCacheDatabase = impl

    @ApplicationScope
    @Provides
    fun providesNotificationsHistoryDatabaseManager(impl: NotificationsHistoryDatabaseImpl): NotificationsHistoryDatabase =
        impl

    @ApplicationScope
    @Provides
    fun providesRideHistoryDatabaseManager(impl: RideHistoryDatabaseManagerImpl): RideHistoryDatabaseManager =
        impl

    @Provides
    fun providesLoggingDatabaseManager(impl: LoggingDatabase): LoggingHelper = impl

    @ApplicationScope
    @Provides
    fun providesLoggingDb(impl: LoggingDatabaseImpl): LoggingDatabase = impl

    @Provides
    fun providesReadSettingsUseCase(ds: SettingsManager): ReadSettingsUseCase =
        ReadSettingsUseCase(ds)

    @Provides
    fun providesWriteSettingsUseCase(ds: SettingsManager): WriteSettingsUseCase =
        WriteSettingsUseCase(ds)
}