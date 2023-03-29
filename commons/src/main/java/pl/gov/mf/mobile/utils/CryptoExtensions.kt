package pl.gov.mf.mobile.utils

import java.math.BigInteger
import java.security.MessageDigest

// sha256 for crypto-req and sha1 as we just need checksum with max length of 50
fun String.checkSum(): String = sha256().sha1()

fun String.sha1(): String = BigInteger(
    1, MessageDigest.getInstance("SHA-1")
        .digest(toByteArray())
)
    .toString(16)
    .padStart(40, '0')

fun String.sha256(): String = BigInteger(
    1, MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
)
    .toString(16)
    .padStart(40, '0')