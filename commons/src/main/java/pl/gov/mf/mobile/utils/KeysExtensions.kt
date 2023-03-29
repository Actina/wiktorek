package pl.gov.mf.mobile.utils

import android.util.Base64
import java.security.PrivateKey
import java.security.Signature

fun PrivateKey.sign(data: String): String? {
    val signature: Signature? = Signature.getInstance("SHA256withRSA")
    signature?.initSign(this)
    signature?.update(data.toByteArray())
    val signed = signature?.sign()
    if (signed != null) {
        //We encode and store in a variable the value of the signature
        return Base64.encodeToString(signed, Base64.DEFAULT)
    }
    return null
}