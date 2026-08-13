package com.systemmonitor.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeyStoreManager @Inject constructor() {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS_PREFIX = "SystemMonitorResourceKey_"
    }

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

    private val ephemeralKeys = java.util.concurrent.ConcurrentHashMap<String, KeyPair>()

    private fun getEphemeralKeyPair(laptopId: String): KeyPair {
        return ephemeralKeys.getOrPut(laptopId) {
            val kpg = KeyPairGenerator.getInstance("EC")
            kpg.initialize(256)
            kpg.generateKeyPair()
        }
    }

    fun getPublicKeyBase64(laptopId: String, useBiometric: Boolean = true): String {
        val keyPair = if (useBiometric) getOrGenerateKeyPair(laptopId) else getEphemeralKeyPair(laptopId)
        return Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
    }

    fun initSignature(laptopId: String, useBiometric: Boolean = true): Signature {
        val signature = Signature.getInstance("SHA256withECDSA")
        if (useBiometric) {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val alias = KEY_ALIAS_PREFIX + laptopId
            
            // Ensure key exists
            getOrGenerateKeyPair(laptopId)
            
            val privateKey = keyStore.getKey(alias, null) as java.security.PrivateKey
            signature.initSign(privateKey)
        } else {
            val keyPair = getEphemeralKeyPair(laptopId)
            signature.initSign(keyPair.private)
        }
        return signature
    }

    fun signPayload(signatureInstance: Signature, payload: String): String {
        signatureInstance.update(payload.toByteArray(Charsets.UTF_8))
        val signatureBytes = signatureInstance.sign()
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }
}
