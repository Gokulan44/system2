package com.systemmonitor.pairing

class PairingCodeScanner {
    fun parseScannedCode(rawText: String): String? {
        val trimmed = rawText.trim()
        return if (trimmed.length == 6 && trimmed.all { it.isDigit() }) {
            trimmed
        } else {
            null
        }
    }
}
