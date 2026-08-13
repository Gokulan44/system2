package com.systemmonitor.core.security

import org.json.JSONObject
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApprovalTokenManager @Inject constructor(
    private val keyStoreManager: AndroidKeyStoreManager
) {

    fun generateSigningString(
        requestId: String,
        laptopId: String,
        resourceId: String,
        resourceName: String,
        createdAt: Long,
        expiresAt: Long,
        nonce: String
    ): String {
        return "$requestId|$laptopId|$resourceId|$resourceName|$createdAt|$expiresAt|$nonce"
    }

    fun createSignedTokenJson(
        requestId: String,
        laptopId: String,
        resourceId: String,
        resourceName: String,
        createdAt: Long,
        expiresAt: Long,
        nonce: String,
        signatureInstance: Signature,
        verificationMethod: String = "BIOMETRIC"
    ): String {
        val signingString = generateSigningString(
            requestId = requestId,
            laptopId = laptopId,
            resourceId = resourceId,
            resourceName = resourceName,
            createdAt = createdAt,
            expiresAt = expiresAt,
            nonce = nonce
        )
        
        val useBiometric = verificationMethod == "BIOMETRIC"
        val finalSignatureInstance = if (useBiometric) {
            signatureInstance
        } else {
            keyStoreManager.initSignature(laptopId, useBiometric = false)
        }
        
        val signatureB64 = keyStoreManager.signPayload(finalSignatureInstance, signingString)
        val publicKeyB64 = keyStoreManager.getPublicKeyBase64(laptopId, useBiometric = useBiometric)

        return JSONObject().apply {
            put("requestId", requestId)
            put("laptopId", laptopId)
            put("resourceId", resourceId)
            put("resourceName", resourceName)
            put("createdAt", createdAt)
            put("expiresAt", expiresAt)
            put("nonce", nonce)
            put("signature", signatureB64)
            put("publicKey", publicKeyB64)
        }.toString()
    }
}
