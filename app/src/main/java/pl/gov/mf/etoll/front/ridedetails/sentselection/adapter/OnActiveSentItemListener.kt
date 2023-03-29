package pl.gov.mf.etoll.front.ridedetails.sentselection.adapter

import pl.gov.mf.etoll.front.ridedetails.sentselection.RideDetailsSentData

interface OnActiveSentItemListener {
    fun onActiveItemDetailsClick(item: RideDetailsSentData)
    fun onActiveItemCancelClick(item: RideDetailsSentData)
    fun onActiveItemFinishClick(item: RideDetailsSentData)
}