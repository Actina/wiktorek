package pl.gov.mf.etoll.front.configsentridesselection.adapter

interface OnSentRideItemListener {
    fun onSentItemClick(item: SentRideItem.SentRideContent)
    fun onSentItemInfoClick(item: SentRideItem.SentRideContent)
}