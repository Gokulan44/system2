package com.systemmonitor.vault.importexport

import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHashManager @Inject constructor() {

    fun calculateSha256(file: File): String {
        return calculateHash(file.inputStream(), "SHA-256")
    }

    fun calculateSha256(inputStream: InputStream): String {
        return calculateHash(inputStream, "SHA-256")
    }

    fun calculateMd5(file: File): String {
        return calculateHash(file.inputStream(), "MD5")
    }

    private fun calculateHash(inputStream: InputStream, algorithm: String): String {
        return inputStream.use { stream ->
            val digest = MessageDigest.getInstance(algorithm)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            digest.digest().fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
