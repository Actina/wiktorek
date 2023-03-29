package pl.gov.mf.etoll.front.ridesummary

import io.reactivex.Completable
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

sealed class SummaryDataUC {

    class CleanSystemAfterSummaryUseCase @Inject constructor(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
        private val rideCoordinatorV3: RideCoordinatorV3,
        private val configurationCoordinator: RideConfigurationCoordinator
    ) :
        SummaryDataUC() {
        fun execute(): Completable = Completable.create { emitter ->
            writeSettingsUseCase.execute(Settings.RIDE_START_TIMESTAMP, "0").subscribe {
                writeSettingsUseCase.execute(Settings.RIDE_END_TIMESTAMP, "0").subscribe {
                    writeSettingsUseCase.execute(Settings.RIDE_SUMMARY_SHOULD_BE_SHOWN, false)
                        .subscribe {
                            rideCoordinatorV3.clearConfiguration()
                            configurationCoordinator.resetCoordinator()
                            emitter.onComplete()
                        }
                }
            }
        }
    }
}