package pl.gov.mf.etoll.security.checker

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class SafetyNetResponse(
    @SerializedName("nonce")
    val nonce: String,
    @SerializedName("timestampMs")
    val timestampMs: String,
    @SerializedName("apkPackageName")
    val apkPackageName: String,
    @SerializedName("apkDigestSha256")
    val apkDigestSha256: String,
    @SerializedName("ctsProfileMatch")
    val isCtsProfileMatch: Boolean = false,
    @SerializedName("apkCertificateDigestSha256")
    val apkCertificateDigestSha256: List<String>,
    @SerializedName("basicIntegrity")
    val isBasicIntegrity: Boolean = false,
    @SerializedName("advice")
    val advice: String? = null,
    @SerializedName("evaluationType")
    val evaluationType: String
) : JsonConvertible
