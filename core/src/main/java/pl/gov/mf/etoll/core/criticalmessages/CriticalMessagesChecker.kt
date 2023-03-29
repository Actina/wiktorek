package pl.gov.mf.etoll.core.criticalmessages

interface CriticalMessagesChecker {
    fun onAppGoesToBackground()
    fun onAppGoesToForeground(criticalMessagesObserver: CriticalMessagesObserver)
    fun intervalCheck()
    fun turnOffValidation()
    fun turnOnValidation()
}