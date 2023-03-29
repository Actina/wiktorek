package pl.gov.mf.etoll.core.ridehistory.model

import androidx.annotation.Keep
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks
import pl.gov.mf.mobile.utils.JsonConvertible

sealed class ActivityAdditionalData : JsonConvertible {

    @Keep
    data class PrePaidAccountTopUp(
        val amountAndCurrency: String,
        val isInitialized: Boolean
    ) : ActivityAdditionalData()

    @Keep
    data class RideSnapshot(
        val rideType: RideCoordinatorV3Callbacks.RideType,
        val monitoringData: MonitoringData,
        val accountInfo: AccountInfoSnapshot?,
        val vehicle: VehicleSnapshot?,
        val categoryIncrease: Boolean,
        val selectedSentList: MutableList<CoreSent>? = null
    ) : ActivityAdditionalData()

    @Keep
    data class SentStartSnapshot(
        val sentNumber: String,
        val monitoringData: MonitoringData,
        val startTimestamp: Int,
        val endTimestamp: Int,
        val loadingAddress: AddressData,
        val deliveryAddress: AddressData
    ) : ActivityAdditionalData()

    @Keep
    data class AddressData(
        val country: String,
        val postalCode: String,
        val city: String,
        val street: String,
        val houseNumber: String,
        val latitude: Double,
        val longitude: Double
    ) : ActivityAdditionalData()

    @Keep
    data class VehicleSnapshot(
        val licensePlate: String,
        val brand: String,
        val model: String,
        val emissionClass: String,
        val category: Int,
    ) : ActivityAdditionalData()

    @Keep
    data class AccountInfoSnapshot(
        val type: String,
        val balanceValue: Double?,
        val balanceIsInitialized: Boolean
    )

    @Keep
    data class MonitoringData(
        val byApp: Boolean
    ) : ActivityAdditionalData()

    @Keep
    data class TrailerData(
        val categoryIncrease: Boolean
    ) : ActivityAdditionalData()
}

