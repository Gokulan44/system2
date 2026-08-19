package com.systemmonitor.securityscan.update

import com.systemmonitor.securityscan.database.entity.KnownHashEntity
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashDatabaseParser @Inject constructor() {
    fun parseJsonFeed(jsonContent: String): List<KnownHashEntity> {
        val list = mutableListOf<KnownHashEntity>()
        try {
            val array = JSONArray(jsonContent)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val sha = obj.getString("sha256").lowercase()
                val type = obj.getString("type").uppercase()
                val app = obj.getString("appName")
                val threat = obj.optString("threatName", "Generic Threat")
                list.add(KnownHashEntity(sha, type, app, threat))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
