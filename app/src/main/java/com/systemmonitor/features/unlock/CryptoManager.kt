package com.systemmonitor.features.unlock

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS_PREFIX = "SystemMonitorUnlockKey_"

    fun getOrGenerateKeyPair(laptopId: String): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val alias = KEY_ALIAS_PREFIX + laptopId

        if (keyStore.containsAlias(alias)) {
            val entry = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
            if (entry != null) {
                return KeyPair(entry.certificate.publicKey, entry.privateKey)
            }
        }

        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(false)
            .build()

        kpg.initialize(spec)
        return kpg.generateKeyPair()
    }

    fun getPublicKeyBase64(laptopId: String): String {
        val keyPair = getOrGenerateKeyPair(laptopId)
        return Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
    }

    fun initSignature(laptopId: String): Signature {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val alias = KEY_ALIAS_PREFIX + laptopId
        val privateKey = keyStore.getKey(alias, null) as java.security.PrivateKey

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        return signature
    }

    fun signChallenge(signatureInstance: Signature, challenge: String): String {
        signatureInstance.update(challenge.toByteArray(Charsets.UTF_8))
        val signatureBytes = signatureInstance.sign()
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }
}
