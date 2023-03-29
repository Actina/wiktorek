package pl.gov.mf.mobile.security.apikey

import android.util.Base64
import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.mobile.utils.JsonConvertible
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec

class GeneratedKeyPair(
) : JsonConvertible {

    companion object {
        private const val KEY_ALGORITHM = "RSA"
    }

    /**
     * Alternative constructor to create all stuff
     * Primary one is used for deserialization purposes
     */
    constructor(
        privateKey: RSAPrivateKey,
        publicKey: RSAPublicKey
    ) : this() {
        privateKeyModulus = privateKey.modulus.toString()
        privateKeyExponent = privateKey.privateExponent.toString()
        publicKeyModulus = publicKey.modulus.toString()
        publicKeyExponent = publicKey.publicExponent.toString()
    }

    @SerializedName("privateModulus")
    private var privateKeyModulus: String = TimeUtils.EMPTY_STRING

    @SerializedName("privateExponent")
    private var privateKeyExponent: String = TimeUtils.EMPTY_STRING

    @SerializedName("publicModulus")
    private var publicKeyModulus: String = TimeUtils.EMPTY_STRING

    @SerializedName("publicExponent")
    private var publicKeyExponent: String = TimeUtils.EMPTY_STRING

    /**
     * Private key deserialization
     */
    val rsaPrivateKey: RSAPrivateKey
        get() {
            val bobPubKeySpec =
                RSAPrivateKeySpec(BigInteger(privateKeyModulus), BigInteger(privateKeyExponent))
            val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
            return keyFactory.generatePrivate(bobPubKeySpec) as RSAPrivateKey
        }

    val rsaPublicKey: RSAPublicKey
        get() {
            val bobPubKeySpec =
                RSAPublicKeySpec(BigInteger(publicKeyModulus), BigInteger(publicKeyExponent))
            val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
            return keyFactory.generatePublic(bobPubKeySpec) as RSAPublicKey
        }

    fun getPublicKeyInPEM(withHeader: Boolean = true): String {
        if (withHeader)
            return "-----BEGIN PUBLIC KEY-----\n" + Base64.encodeToString(
                rsaPublicKey.encoded, Base64.DEFAULT
            ) + "-----END PUBLIC KEY-----"
        else
            return Base64.encodeToString(rsaPublicKey.encoded, Base64.DEFAULT)
    }
}