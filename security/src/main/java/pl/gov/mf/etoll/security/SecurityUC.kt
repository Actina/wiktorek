package pl.gov.mf.etoll.security

import androidx.security.crypto.MasterKey
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.security.checker.checksum.AppChecksumChecker
import pl.gov.mf.mobile.security.apikey.GeneratedKeyPair
import pl.gov.mf.mobile.security.apikey.KeyPairGenerator
import pl.gov.mf.mobile.security.deviceId.DeviceIdProvider
import pl.gov.mf.mobile.security.masterkey.MasterKeyProvider
import pl.gov.mf.mobile.utils.checkSum

sealed class SecurityUC {
    class GenerateDeviceIdUseCase(private val deviceIdProvider: DeviceIdProvider) : SecurityUC() {
        fun execute(): String = "android_" + deviceIdProvider.getDeviceId().checkSum()
    }

    class GetAppSigningUseCase(private val checksumChecker: AppChecksumChecker) :
        SecurityUC() {
        fun execute(): String = checksumChecker.getChecksum()
        fun executeAsync(): Single<String> = Single.just(execute())
    }

    class GenerateKeyPairUseCase(private val keyPairGenerator: KeyPairGenerator) :
        SecurityUC() {
        fun execute(size: Int = 2048): GeneratedKeyPair = keyPairGenerator.generateKeyPair(size)
        fun executeAsync(size: Int = 2048): Single<GeneratedKeyPair> = Single.just(execute(size))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class GetMasterKeyUseCase(private val masterKeyProvider: MasterKeyProvider) :
        SecurityUC() {
        fun execute(keyAlias: String): Single<MasterKey> = masterKeyProvider.getMasterKey(keyAlias)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        fun executeSynch(keyAlias: String): MasterKey =
            masterKeyProvider.getMasterKeySynch(keyAlias)

    }

}