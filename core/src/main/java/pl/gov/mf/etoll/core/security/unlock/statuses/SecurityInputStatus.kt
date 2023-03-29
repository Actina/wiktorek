package pl.gov.mf.etoll.core.security.unlock.statuses

sealed class SecurityInputStatus {
    object None : SecurityInputStatus()
    object Valid : SecurityInputStatus()
    object Unlocked : SecurityInputStatus()
    data class Locked(val timeLeftInMillis: Long) : SecurityInputStatus()
    data class InvalidAndLocked(val timeLeftInMillis: Long) : SecurityInputStatus()
    data class InvalidAndUnlocked(val attemptsLeft: Int) : SecurityInputStatus()

    fun isLockedStatus(): Boolean = this is Locked || this is InvalidAndLocked
}