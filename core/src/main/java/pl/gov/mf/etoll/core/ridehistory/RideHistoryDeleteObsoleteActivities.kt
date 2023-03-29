package pl.gov.mf.etoll.core.ridehistory

import io.reactivex.Single
import pl.gov.mf.etoll.initialization.LoadableSystem
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import javax.inject.Inject

class RideHistoryDeleteObsoleteActivities @Inject constructor(
    private val deleteObsoleteUseCase: RideHistoryUC.DeleteObsoleteUseCase
) : LoadableSystem {
    override fun load(): Single<Boolean> =
        deleteObsoleteUseCase.execute()

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        // do nothing
    }
}