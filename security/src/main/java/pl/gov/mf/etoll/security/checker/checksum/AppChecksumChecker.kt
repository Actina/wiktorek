package pl.gov.mf.etoll.security.checker.checksum

interface AppChecksumChecker {

    /**
     * Validate checksum known to app and one created dynamically
     */
    fun isChecksumCorrect(): Boolean

    /**
     * Get current checksum
     */
    fun getChecksum(): String
}