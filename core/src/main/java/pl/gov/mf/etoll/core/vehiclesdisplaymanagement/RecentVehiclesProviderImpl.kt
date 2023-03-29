package pl.gov.mf.etoll.core.vehiclesdisplaymanagement

import io.reactivex.Single
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.toObject

class RecentVehiclesProviderImpl(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val getCoreStatusUseCase: CoreComposedUC.GetCoreStatusUseCase
) : RecentVehiclesProvider {

    override fun getVehiclesDividedIntoRecentAndOther(): Single<Pair<List<CoreVehicle>, List<CoreVehicle>>> =
        Single.create {
            getCoreStatusUseCase.executeAsync().subscribe({ status ->
                val statusVehicles: List<CoreVehicle> = status.vehicles.map { it }

                readSettingsUseCase.executeForStringAsync(Settings.RECENT_VEHICLES)
                    .subscribe({ storedRecentVehiclesBuffer ->
                        val buffer = getBuffer(storedRecentVehiclesBuffer)
                        val previouslyBufferedRecentsIds = buffer.recentVehiclesIds
                        if (previouslyBufferedRecentsIds.isEmpty()) {
                            it.onSuccess(Pair(emptyList(), statusVehicles))
                        } else {
                            //Spli vehicles obtained from server to recents and others
                            val (newUnsortedRecents, newUnsortedOthers) = statusVehicles.partition { statusVehicle ->
                                previouslyBufferedRecentsIds.contains(statusVehicle.id)
                            }
                            //Sort recents according to usage order kept by recents' buffer
                            val sortedRecents = newUnsortedRecents.sortedBy { unsortedRecent ->
                                previouslyBufferedRecentsIds.indexOf(unsortedRecent.id)
                            }
                            it.onSuccess(Pair(sortedRecents, newUnsortedOthers))
                        }
                    }, {
                        //do nothing
                    })
            }, {
                //do nothing
            })
        }

    override fun addRecentVehicle(coreVehicleId: Long) =
        readSettingsUseCase.executeForStringAsync(Settings.RECENT_VEHICLES)
            .flatMapCompletable { storedRecentVehiclesBuffer ->
                val buffer = getBuffer(storedRecentVehiclesBuffer)
                buffer.add(coreVehicleId)
                writeSettingsUseCase.execute(
                    Settings.RECENT_VEHICLES,
                    buffer.toJSON()
                )
            }

    private fun getBuffer(storedRecentVehiclesBuffer: String) =
        try {
            if (storedRecentVehiclesBuffer.isEmpty()) RecentVehiclesBuffer()
            else storedRecentVehiclesBuffer.toObject()
        } catch (ex: Exception) {
            RecentVehiclesBuffer()
        }
}

