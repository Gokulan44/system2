package com.systemmonitor.data.network

import com.systemmonitor.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared JSON parsing helpers used by both ApiClient (local) and RemoteRelayManager (Firestore relay).
 */
object ApiClientParser {

    fun parseTelemetryJson(root: JSONObject): UsageInfo {
        val cpuObj = root.optJSONObject("cpu") ?: JSONObject()
        val perCpuArray = cpuObj.optJSONArray("per_cpu_usage")
        val perCpuList = mutableListOf<Double>()
        if (perCpuArray != null) {
            for (i in 0 until perCpuArray.length()) perCpuList.add(perCpuArray.optDouble(i, 0.0))
        }
        val cpu = CpuInfo(
            processorName = cpuObj.optString("processor_name", "Unknown"),
            usagePercent = cpuObj.optDouble("usage_percent", 0.0),
            logicalCores = cpuObj.optInt("logical_cores", 4),
            physicalCores = cpuObj.optInt("physical_cores", 2),
            frequencyMhz = cpuObj.optDouble("frequency_mhz", 2400.0),
            maxFrequencyMhz = cpuObj.optDouble("max_frequency_mhz", 5000.0),
            perCpuUsage = perCpuList
        )

        val memObj = root.optJSONObject("memory") ?: JSONObject()
        val mem = MemoryInfo(
            totalBytes = memObj.optLong("total_bytes", 0),
            availableBytes = memObj.optLong("available_bytes", 0),
            usedBytes = memObj.optLong("used_bytes", 0),
            freeBytes = memObj.optLong("free_bytes", 0),
            usagePercent = memObj.optDouble("usage_percent", 0.0),
            swapTotalBytes = memObj.optLong("swap_total_bytes", 0),
            swapUsedBytes = memObj.optLong("swap_used_bytes", 0),
            swapPercent = memObj.optDouble("swap_percent", 0.0)
        )

        val batObj = root.optJSONObject("battery") ?: JSONObject()
        val bat = LaptopBatteryInfo(
            hasBattery = batObj.optBoolean("has_battery", true),
            percent = batObj.optDouble("percent", 100.0),
            powerPlugged = batObj.optBoolean("power_plugged", true),
            timeRemainingSeconds = batObj.optLong("time_remaining_seconds", -1),
            status = batObj.optString("status", "AC Power")
        )

        val storageObj = root.optJSONObject("storage") ?: JSONObject()
        val partitionsArray = storageObj.optJSONArray("partitions") ?: JSONArray()
        val partitionsList = mutableListOf<StoragePartitionInfo>()
        for (i in 0 until partitionsArray.length()) {
            val p = partitionsArray.getJSONObject(i)
            partitionsList.add(
                StoragePartitionInfo(
                    device = p.optString("device", ""),
                    mountpoint = p.optString("mountpoint", ""),
                    fstype = p.optString("fstype", ""),
                    totalBytes = p.optLong("total_bytes", 0),
                    usedBytes = p.optLong("used_bytes", 0),
                    freeBytes = p.optLong("free_bytes", 0),
                    usagePercent = p.optDouble("usage_percent", 0.0)
                )
            )
        }
        val storage = StorageInfo(
            overallTotalBytes = storageObj.optLong("overall_total_bytes", 0),
            overallUsedBytes = storageObj.optLong("overall_used_bytes", 0),
            overallFreeBytes = storageObj.optLong("overall_free_bytes", 0),
            overallUsagePercent = storageObj.optDouble("overall_usage_percent", 0.0),
            partitions = partitionsList
        )

        val netObj = root.optJSONObject("network") ?: JSONObject()
        val ifacesArray = netObj.optJSONArray("interfaces") ?: JSONArray()
        val ifacesList = mutableListOf<NetworkInterfaceInfo>()
        for (i in 0 until ifacesArray.length()) {
            val n = ifacesArray.getJSONObject(i)
            ifacesList.add(NetworkInterfaceInfo(
                interfaceName = n.optString("interface_name", ""),
                ipAddress = n.optString("ip_address", "")
            ))
        }
        val net = LaptopNetworkInfo(
            hostname = netObj.optString("hostname", "Laptop"),
            primaryIp = netObj.optString("primary_ip", ""),
            bytesSent = netObj.optLong("bytes_sent", 0),
            bytesRecv = netObj.optLong("bytes_recv", 0),
            packetsSent = netObj.optLong("packets_sent", 0),
            packetsRecv = netObj.optLong("packets_recv", 0),
            interfaces = ifacesList
        )

        return UsageInfo(
            cpu = cpu,
            memory = mem,
            storage = storage,
            battery = bat,
            network = net,
            uptimeSeconds = root.optDouble("uptime_seconds", 0.0),
            isLocked = root.optBoolean("locked", true)
        )
    }

    fun parseProcessesJson(json: String): List<ProcessInfo> {
        val array = JSONArray(json)
        val list = mutableListOf<ProcessInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(ProcessInfo(
                pid = obj.optInt("pid", 0),
                name = obj.optString("name", "process"),
                cpuPercent = obj.optDouble("cpu_percent", 0.0),
                memoryPercent = obj.optDouble("memory_percent", 0.0),
                status = obj.optString("status", "running"),
                username = obj.optString("username", "")
            ))
        }
        return list
    }
}
