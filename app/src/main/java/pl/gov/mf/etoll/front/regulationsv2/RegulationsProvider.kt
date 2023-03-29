package pl.gov.mf.etoll.front.regulationsv2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.networking.RegistrationProvider
import pl.gov.mf.etoll.networking.manager.NetworkingManagerV2
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC

abstract class RegulationsProvider(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val registrationProvider: RegistrationProvider,
    private val checkAutoTimeEnabledUseCase: DeviceCompatibilityUC.CheckAutoTimeEnabledUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    abstract fun getRegulationsFileName(): String

    suspend fun cleanAllAcceptedRegulations() {
        writeSettingsUseCase.executeV2(Settings.ACCEPTED_REGULATIONS_FILENAME, "")
    }

    /**
     * Get info if there is anything to accept
     */
    suspend fun getCurrentRegulationsMode(): Mode {
        val lastFileName =
            readSettingsUseCase.executeForStringV2(settings = Settings.ACCEPTED_REGULATIONS_FILENAME)
        val businessNumber =
            readSettingsUseCase.executeForStringV2(settings = Settings.BUSINESS_NUMBER)
        if (lastFileName.isEmpty()) {
            return if (businessNumber.isEmpty())
                Mode.REGISTRATION_REQUIRED
            else
                Mode.NEW_REGULATIONS
        }
        return if (lastFileName.compareTo(getRegulationsFileName()) != 0) Mode.NEW_REGULATIONS else Mode.ALL_ACCEPTED
    }

    /**
     * Explicitly accept new regulations
     */
    suspend fun acceptNewRegulations(
        job: Job,
        returnDelegate: () -> Unit,
        errorDelegate: (NetworkingManagerV2.NetworkErrorCause) -> Unit,
    ) {
        when (getCurrentRegulationsMode()) {
            Mode.ALL_ACCEPTED -> throw RuntimeException("Can't accept regulations, bug in flow!")
            Mode.NEW_REGULATIONS -> {
                // accept new regulations
                scope.launch(Dispatchers.IO + job) {
                    writeSettingsUseCase.executeV2(
                        settings = Settings.ACCEPTED_REGULATIONS_FILENAME,
                        value = getRegulationsFileName()
                    )
                    scope.launch(Dispatchers.Main + job) {
                        returnDelegate()
                    }
                }
            }

            Mode.REGISTRATION_REQUIRED -> {
                // do the registration
                // cases:
                // - custom time setup -> time issue (TODO: should it still be here? )
                // - no firebase ID -> general error (done in networking manager v2)
                // - time issue -> time issue dialog
                // - version issue -> version issue dialog
                if (checkAutoTimeEnabledUseCase.execute()) {
                    registrationProvider.register(
                        job = job,
                        returnDelegate = {
                            scope.launch(Dispatchers.IO + job) {
                                writeSettingsUseCase.executeV2(
                                    settings = Settings.ACCEPTED_REGULATIONS_FILENAME,
                                    value = getRegulationsFileName()
                                )
                                // apply current language, or more precise - do not allow to override it again on start
                                writeSettingsUseCase.executeV2(
                                    Settings.SELECTED_LANGUAGE_WEAK_SAVE,
                                    false
                                )
                                scope.launch(Dispatchers.Main + job) {
                                    returnDelegate()
                                }
                            }
                        },
                        errorDelegate = {
                            scope.launch(Dispatchers.Main + job) {
                                errorDelegate(it)
                            }
                        }
                    )
                } else {
                    scope.launch(Dispatchers.Main + job) {
                        errorDelegate(NetworkingManagerV2.NetworkErrorCause.TIME_PROBLEM)
                    }
                }
            }
        }
    }

    enum class Mode {
        ALL_ACCEPTED, NEW_REGULATIONS, REGISTRATION_REQUIRED
    }
}