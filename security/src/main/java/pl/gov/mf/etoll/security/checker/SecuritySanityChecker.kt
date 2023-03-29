package pl.gov.mf.etoll.security.checker

import android.content.Intent
import android.os.Bundle
import io.reactivex.Single

interface SecuritySanityChecker {

    /**
     * Add signing to intent to mark it as sent from our ecosystem
     */
    fun sign(intent: Intent): Intent

    /**
     * Add signing to bundle used as fragment args to mark it as created from our ecosystem
     */
    fun sign(args: Bundle): Bundle

    /**
     * Sanity check for intents, warning - this should not check app start intent
     */
    fun checkIfIntentIsSigned(intent: Intent): Boolean

    /**
     * Sanity check for fragments argument bundle
     */
    fun checkIfBundleIsSigned(args: Bundle): Boolean

    /**
     * Do all checks for security and return report in case of issues
     */
    fun validateSecuritySanity(testMode: Boolean = false): Single<List<SecurityIssues>>

    fun getSigning(): String
}