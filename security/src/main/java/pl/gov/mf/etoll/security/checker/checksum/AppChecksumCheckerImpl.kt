package pl.gov.mf.etoll.security.checker.checksum

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import pl.gov.mf.etoll.security.BuildConfig
import java.security.MessageDigest
import javax.inject.Inject

class AppChecksumCheckerImpl @Inject constructor(private val context: Context) :
    AppChecksumChecker {

    override fun isChecksumCorrect(): Boolean {
        if (BuildConfig.ALLOW_DBG) return true
        val checksum = getChecksum()
        return checksum.isNotEmpty() && BuildConfig.KNOWN_CHECKSUM.contentEquals(checksum)
    }

    override fun getChecksum(): String = StringBuilder().apply {
        getApplicationSignature().forEach { append(it) }
    }
        .toString()

    @SuppressLint("PackageManagerGetSignatures")
    private fun getApplicationSignature(packageName: String = context.packageName): List<String> {
        val signatureList: List<String>
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // New signature
                val sig = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo
                signatureList = if (sig.hasMultipleSigners()) {
                    // Send all with apkContentsSigners
                    sig.apkContentsSigners.map(this::signatureToHex)
                } else {
                    // Send one with signingCertificateHistory
                    sig.signingCertificateHistory.map(this::signatureToHex)
                }
            } else {
                val sig = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
                signatureList = sig.map(this::signatureToHex)
            }

            return signatureList
        } catch (e: Exception) {
            // atm no action, just return empty list
        }
        return emptyList()
    }

    private fun signatureToHex(it: Signature): String {
        val digest = MessageDigest.getInstance("SHA")
        digest.update(it.toByteArray())
        return bytesToHex(digest.digest())
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray =
            charArrayOf(
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
            )
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}