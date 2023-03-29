package pl.gov.mf.etoll.security.checker

import android.content.Intent
import android.os.Bundle
import io.reactivex.Single
import pl.gov.mf.etoll.security.checker.SecurityIssues.*
import pl.gov.mf.etoll.security.checker.checksum.AppChecksumChecker
import pl.gov.mf.etoll.security.checker.debug.DebugChecker
import pl.gov.mf.etoll.security.checker.root.DeviceRootChecker
import java.util.*
import javax.inject.Inject

class SecuritySanityCheckerImpl @Inject constructor(
    private val debugChecker: DebugChecker,
    private val checksumChecker: AppChecksumChecker,
    private val deviceRootChecker: DeviceRootChecker
) :
    SecuritySanityChecker {

    companion object {
        private const val SIGNING_EXTENSION = "signing"

        // TODO: this got simplified atm to prevent issue when application gets recreated during ride
        private const val SIGNING_EXTENSION_VALUE = "002021040100"
    }

    override fun sign(intent: Intent): Intent = intent.apply {
        putExtra(SIGNING_EXTENSION, SIGNING_EXTENSION_VALUE)
    }

    override fun sign(args: Bundle): Bundle = args.apply {
        putString(SIGNING_EXTENSION, SIGNING_EXTENSION_VALUE)
    }

    override fun checkIfIntentIsSigned(intent: Intent): Boolean =
        intent.extras != null && checkIfBundleIsSigned(intent.extras!!)

    override fun checkIfBundleIsSigned(args: Bundle): Boolean =
        args.containsKey(SIGNING_EXTENSION) &&
                args.getString(SIGNING_EXTENSION)!!
                    .contentEquals(SIGNING_EXTENSION_VALUE)

    override fun getSigning(): String = SIGNING_EXTENSION_VALUE

    override fun validateSecuritySanity(testMode: Boolean): Single<List<SecurityIssues>> {
        return Single.create { emitter ->
            val output = Vector<SecurityIssues>()
            if (debugChecker.checkIfDebuggingIsEnabled(testMode))
                output.addElement(DEBUG_MODE)
            if (!checksumChecker.isChecksumCorrect())
                output.addElement(MALFORMED_APK)
            if (deviceRootChecker.isRoot().blockingGet())
                output.addElement(ROOT)

            emitter.onSuccess(output)
        }
    }
}