package pl.gov.mf.etoll.security.checker.debug

interface DebugChecker {
    fun checkIfDebuggingIsEnabled(testMode: Boolean = false): Boolean
}