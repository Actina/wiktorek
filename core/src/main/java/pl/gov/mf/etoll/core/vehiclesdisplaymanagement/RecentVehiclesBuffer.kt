package pl.gov.mf.etoll.core.vehiclesdisplaymanagement

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class RecentVehiclesBuffer(
    @SerializedName("bufferSize") val bufferSize: Int = 3,
    @SerializedName("recentVehicles") val recentVehiclesIds: ArrayList<Long> = arrayListOf()
) : JsonConvertible

fun RecentVehiclesBuffer.add(vehicleId: Long): RecentVehiclesBuffer {
    if (recentVehiclesIds.contains(vehicleId)) {
        recentVehiclesIds.remove(vehicleId)
    }

    if (recentVehiclesIds.size == bufferSize) {
        recentVehiclesIds.add(0, vehicleId)
        recentVehiclesIds.removeLast()
    } else {
        recentVehiclesIds.add(0, vehicleId)
    }
    return this
}