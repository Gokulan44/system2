package com.systemmonitor.core

/**
 * Single source of truth for which external services this app talks to.
 *
 * INTENTIONALLY the only third-party backend is Firebase (Auth, Firestore,
 * Storage, Messaging) via google-services.json — configured entirely
 * through the Firebase SDKs, so no separate API key constant is needed here.
 *
 * This app does NOT call, and should never be wired to: Google Maps,
 * VirusTotal, AbuseIPDB, Google Safe Browsing, IP Geolocation, Weather, or
 * HaveIBeenPwned APIs. Security/threat scoring (see SecurityScoreEngine) is
 * done entirely on-device with local heuristics — permission analysis and
 * install-source checks — not third-party lookups. If a future feature
 * genuinely needs one of these services, add the key here explicitly next
 * to a comment naming the feature that needs it; don't add speculative keys.
 */
object AppConfig {
    const val MIN_SECURITY_SCORE_FOR_GOOD_STANDING = 70
}
