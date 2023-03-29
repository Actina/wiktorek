package pl.gov.mf.mobile.security.apikey

import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.inject.Inject

class KeyPairGeneratorImpl @Inject constructor() :
    pl.gov.mf.mobile.security.apikey.KeyPairGenerator {

    companion object {
        private const val CRYPTO_METHOD = "RSA"
    }

    override fun generateKeyPair(size: Int): GeneratedKeyPair {
        val kpg: KeyPairGenerator =
            KeyPairGenerator.getInstance(CRYPTO_METHOD)
        kpg.initialize(size)
        val keyPair = kpg.generateKeyPair()
        return GeneratedKeyPair(keyPair.private as RSAPrivateKey, keyPair.public as RSAPublicKey)
    }
}