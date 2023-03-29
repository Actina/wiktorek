package pl.gov.mf.etoll.networking.manager.netswitch

interface NetworkSwitchConditionsCheck {
    fun shouldSelectRealManager(): Boolean
    fun lockToDummy()
}