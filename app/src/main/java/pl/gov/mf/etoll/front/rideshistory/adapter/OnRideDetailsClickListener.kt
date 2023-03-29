package pl.gov.mf.etoll.front.rideshistory.adapter

import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem


interface OnRideDetailsClickListener {
    fun onRideDetailsClick(item: RideHistoryDataItem)
}