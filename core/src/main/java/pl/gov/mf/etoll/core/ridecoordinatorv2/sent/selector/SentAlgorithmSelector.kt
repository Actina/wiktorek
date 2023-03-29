package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.selector

import android.util.Log
import pl.gov.mf.etoll.core.model.CoreConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.SentAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.SimpleSentAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.SentAdvancedAlgorithm
import pl.gov.mf.mobile.utils.LocationWrapper
import javax.inject.Inject

class SentAlgorithmSelector @Inject constructor(
    private val simpleAlg: SimpleSentAlgorithm,
    private val advAlg: SentAdvancedAlgorithm,
) : SentAlgorithm {

    companion object {
        /**
         * Static method for updating static params
         */
        fun updateConfiguration(configuration: CoreConfiguration) {
            selectedAdvancedAlgorithm = configuration.sentAlgorithmUsed
            SentAdvancedAlgorithm.updateConfiguration(configuration)
            Log.d(
                "SENT_ALG",
                "UPDATING CONFIGURATION, will use advanced = $selectedAdvancedAlgorithm"
            )
        }

        private var selectedAdvancedAlgorithm = true
    }

    private var modeForCurrentRideIsAdvanced = true

    override fun onNextSecond(location: LocationWrapper?): Boolean =
        if (modeForCurrentRideIsAdvanced) {
            advAlg.onNextSecond(location)
        } else
            simpleAlg.onNextSecond(location)

    override fun resetAlgorithm() {
        modeForCurrentRideIsAdvanced = selectedAdvancedAlgorithm
        if (modeForCurrentRideIsAdvanced) {
            advAlg.resetAlgorithm()
        } else
            simpleAlg.resetAlgorithm()
    }
}