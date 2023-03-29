package pl.gov.mf.mobile.security.apikey

interface KeyPairGenerator {
    fun generateKeyPair(size: Int): GeneratedKeyPair
}