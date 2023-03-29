package pl.gov.mf.etoll.networking

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.jwt.JwtToken
import pl.gov.mf.mobile.networking.api.jwt.JwtTokenGenerator
import pl.gov.mf.mobile.security.apikey.GeneratedKeyPair
import pl.gov.mf.mobile.utils.toObject
import java.security.PrivateKey
import javax.inject.Inject

sealed class NetworkingUC {

    class GenerateJwtTokenUseCase @Inject constructor(private val tokenGenerator: JwtTokenGenerator) :
        NetworkingUC() {
        fun execute(
            applicationId: String,
            privateKey: PrivateKey
        ): JwtToken {
            return tokenGenerator.generate(applicationId, privateKey)
        }
    }

    class GetFirebaseUserIdUseCase @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
        NetworkingUC() {
        fun execute(): Single<String> =
            readSettingsUseCase.executeForStringAsync(Settings.FIREBASE_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    class GetApiKeysUseCase @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
        NetworkingUC() {
        fun execute() = readSettingsUseCase.executeForStringAsync(Settings.API_KEYS)
            .map { it.toObject<GeneratedKeyPair>() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class GetBusinessNumberUseCase @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
        NetworkingUC() {
        fun execute() = readSettingsUseCase.executeForStringAsync(Settings.BUSINESS_NUMBER)
            .map { if (it.isEmpty()) throw IllegalStateException("No business number") else it }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class CleanBusinessNumberUseCase @Inject constructor(private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) :
        NetworkingUC() {
        fun execute() =
            writeSettingsUseCase.execute(Settings.BUSINESS_NUMBER, TimeUtils.EMPTY_STRING)
                .andThen(
                    writeSettingsUseCase.execute(
                        Settings.GENERATED_JWT_TOKEN,
                        TimeUtils.EMPTY_STRING
                    )
                )
                .andThen(
                    writeSettingsUseCase.execute(
                        Settings.RECEIVED_ACCESS_TOKEN,
                        TimeUtils.EMPTY_STRING
                    )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}