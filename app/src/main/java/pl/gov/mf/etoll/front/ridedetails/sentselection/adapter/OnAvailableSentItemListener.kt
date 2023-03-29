package pl.gov.mf.etoll.front.ridedetails.sentselection.adapter

interface OnAvailableSentItemListener {
    fun onSentItemClick(item: SentRideItem.AvailableSentContent)
    fun onAvailableItemDetailsClick(item: SentRideItem.AvailableSentContent)
}