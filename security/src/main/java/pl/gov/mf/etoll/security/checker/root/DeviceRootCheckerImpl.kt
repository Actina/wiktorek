package pl.gov.mf.etoll.security.checker.root

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import com.scottyab.rootbeer.RootBeer
import io.reactivex.Single
import pl.gov.mf.etoll.security.BuildConfig
import pl.gov.mf.etoll.security.checker.SafetyNetResponse
import pl.gov.mf.mobile.utils.toObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject

class DeviceRootCheckerImpl @Inject constructor(private val applicationContext: Context) :
    DeviceRootChecker {
    companion object {
        const val RANDOM_BYTES_COUNT = 24
        val SAFETY_NET_TAG = DeviceRootCheckerImpl::class.java.simpleName
        private const val JWT_PARTS_COUNT = 3
    }

    override fun isRoot(): Single<Boolean> = Single.create { emitter ->
        if (BuildConfig.ALLOW_DBG) {
            emitter.onSuccess(false)
            return@create
        }
        val disableOnlineSafetyNetMode = true
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS &&
            !disableOnlineSafetyNetMode
        ) {
            //Play services are available
            val nonceData = SAFETY_NET_TAG + System.currentTimeMillis()
            val nonce: ByteArray? = getRequestNonce(nonceData)
            if (nonce != null) {
                if (BuildConfig.ANDROID_DEVICE_VERIFICATION_API_KEY.isEmpty()) {
                    throw RuntimeException("Missing android device verification api key!")
                }
                SafetyNet.getClient(applicationContext)
                    .attest(nonce, BuildConfig.ANDROID_DEVICE_VERIFICATION_API_KEY)
                    .addOnSuccessListener {
                        val response = parseJsonWebSignature(it.jwsResult)
                        if (response == null) {
                            //Cannot parse response
                            emitter.onSuccess(checkRootUsingRootBeer(applicationContext))
                        } else {
                            if (!Arrays.equals(
                                    nonce,
                                    Base64.decode(response.nonce, Base64.DEFAULT)
                                )
                            ) {
                                //Received nonce is different than sent nonce - abuse detected -
                                //we should check root other way
                                emitter.onSuccess(checkRootUsingRootBeer(applicationContext))
                            } else {
                                val rooted =
                                    !(response.isBasicIntegrity && response.isCtsProfileMatch)
                                emitter.onSuccess(rooted)
                                Log.d(
                                    DeviceRootCheckerImpl::class.java.simpleName,
                                    "Root checked using SafetyNet, rooted: $rooted, advice: ${response.advice}"
                                )
                            }
                        }
                    }.addOnFailureListener {
                        //E.g. missing network
                        emitter.onSuccess(checkRootUsingRootBeer(applicationContext))
                    }
            } else {
                //Cannot create nonce
                emitter.onSuccess(checkRootUsingRootBeer(applicationContext))
            }
        } else {
            //Play services unavailable or online
            emitter.onSuccess(checkRootUsingRootBeer(applicationContext))
        }
    }

    private fun checkRootUsingRootBeer(context: Context): Boolean =
        RootBeer(context).isRooted.also {
            Log.d(
                DeviceRootCheckerImpl::class.java.simpleName,
                "Root checked using RootBeer, rooted: $it"
            )
        }

    private fun parseJsonWebSignature(jwsResult: String?): SafetyNetResponse? {
        if (jwsResult == null) {
            return null
        }
        val jwtParts = jwsResult.split(".").toTypedArray()
        return if (jwtParts.size == JWT_PARTS_COUNT) {
            String(Base64.decode(jwtParts[1], Base64.DEFAULT), Charset.forName("UTF-8")).toObject()
        } else {
            null
        }
    }

    private fun getRequestNonce(data: String): ByteArray? {
        val bytes = ByteArray(RANDOM_BYTES_COUNT)
        SecureRandom().nextBytes(bytes)
        return ByteArrayOutputStream().apply {
            try {
                write(bytes)
                write(data.toByteArray())
            } catch (e: IOException) {
                return null
            }
        }.toByteArray()
    }
}