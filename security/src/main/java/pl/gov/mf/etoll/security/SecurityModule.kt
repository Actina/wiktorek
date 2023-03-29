package pl.gov.mf.etoll.security

import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.etoll.security.SecurityUC.*
import pl.gov.mf.etoll.security.checker.SecuritySanityChecker
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerImpl
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase.*
import pl.gov.mf.etoll.security.checker.checksum.AppChecksumChecker
import pl.gov.mf.etoll.security.checker.checksum.AppChecksumCheckerImpl
import pl.gov.mf.etoll.security.checker.debug.DebugChecker
import pl.gov.mf.etoll.security.checker.debug.DebugCheckerImpl
import pl.gov.mf.etoll.security.checker.root.DeviceRootChecker
import pl.gov.mf.etoll.security.checker.root.DeviceRootCheckerImpl
import pl.gov.mf.mobile.security.apikey.KeyPairGenerator
import pl.gov.mf.mobile.security.apikey.KeyPairGeneratorImpl
import pl.gov.mf.mobile.security.deviceId.DeviceIdProvider
import pl.gov.mf.mobile.security.deviceId.DeviceIdProviderImpl
import pl.gov.mf.mobile.security.deviceId.generators.IdGenerators
import pl.gov.mf.mobile.security.masterkey.MasterKeyProvider
import pl.gov.mf.mobile.security.masterkey.MasterKeyProviderImpl

@Module
class SecurityModule {

    @ApplicationScope
    @Provides
    fun providesDeviceIdProvider(impl: DeviceIdProviderImpl): DeviceIdProvider = impl

    @ApplicationScope
    @Provides
    fun providesSecuritySanityChecker(impl: SecuritySanityCheckerImpl): SecuritySanityChecker = impl

    @ApplicationScope
    @Provides
    fun providesDebugChecker(impl: DebugCheckerImpl): DebugChecker = impl

    @ApplicationScope
    @Provides
    fun providesChecksumChecker(impl: AppChecksumCheckerImpl): AppChecksumChecker = impl

    @ApplicationScope
    @Provides
    fun providesMediaDrmDeviceIdGenerator() = IdGenerators.MediaDrmDeviceIdGenerator()

    @ApplicationScope
    @Provides
    fun providesSecureIdDeviceIdGenerator(contentResolver: ContentResolver) =
        IdGenerators.SecureIdDeviceIdGenerator(contentResolver)

    @ApplicationScope
    @Provides
    fun providesBuildSerialDeviceIdGenerator() = IdGenerators.BuildSerialDeviceIdGenerator()

    @ApplicationScope
    @Provides
    fun providesPseudoUniqueDeviceIdGenerator() = IdGenerators.PseudoUniqueDeviceIdGenerator()

    @ApplicationScope
    @Provides
    fun providesApiKeyGenerator(impl: KeyPairGeneratorImpl): KeyPairGenerator = impl

    @ApplicationScope
    @Provides
    fun providesDeviceRootChecker(impl: DeviceRootCheckerImpl): DeviceRootChecker = impl

    @Provides
    fun providesGetDeviceIdUseCase(impl: DeviceIdProvider): GenerateDeviceIdUseCase =
        GenerateDeviceIdUseCase(impl)

    @Provides
    fun provideCheckIfDeviceIsRootedUseCase(ds: DeviceRootChecker): CheckIfDeviceIsRootedUseCase =
        CheckIfDeviceIsRootedUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesMasterKeyProvider(impl: MasterKeyProviderImpl): MasterKeyProvider = impl

    @Provides
    fun providesSignAndroidComponentUseCase(ds: SecuritySanityChecker): SignAndroidComponentUseCase =
        SignAndroidComponentUseCase(ds)

    @Provides
    fun providesValidateSecuritySanityUseCase(ds: SecuritySanityChecker): ValidateSecuritySanityUseCase =
        ValidateSecuritySanityUseCase(ds)

    @Provides
    fun providesValidateSigningUseCase(ds: SecuritySanityChecker): ValidateAndroidComponentSigningUseCase =
        ValidateAndroidComponentSigningUseCase(ds)

    @Provides
    fun getAppSigningUseCase(ds: AppChecksumChecker): GetAppSigningUseCase =
        GetAppSigningUseCase(ds)

    @Provides
    fun providesGenerateApiKeyUseCase(ds: KeyPairGenerator): GenerateKeyPairUseCase =
        GenerateKeyPairUseCase(ds)

    @Provides
    fun providesGetMasterKeyUseCase(ds: MasterKeyProvider): GetMasterKeyUseCase =
        GetMasterKeyUseCase(ds)

}
