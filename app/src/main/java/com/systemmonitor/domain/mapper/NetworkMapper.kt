package com.systemmonitor.domain.mapper

import com.systemmonitor.domain.model.NetworkInfo
import com.systemmonitor.domain.model.TransportType
import com.systemmonitor.domain.model.WifiInfo
import com.systemmonitor.local.database.entity.NetworkEntity
import com.systemmonitor.local.database.entity.WifiEntity

fun NetworkEntity.toDomain(): NetworkInfo = NetworkInfo(
    timestamp = timestamp,
    isConnected = isConnected,
    transportType = runCatching { TransportType.valueOf(transportType) }.getOrDefault(TransportType.NONE),
    isMetered = isMetered,
    downstreamKbps = downstreamKbps,
    upstreamKbps = upstreamKbps
)

fun NetworkInfo.toEntity(): NetworkEntity = NetworkEntity(
    timestamp = timestamp,
    isConnected = isConnected,
    transportType = transportType.name,
    isMetered = isMetered,
    downstreamKbps = downstreamKbps,
    upstreamKbps = upstreamKbps
)

fun WifiEntity.toDomain(): WifiInfo = WifiInfo(
    timestamp = timestamp,
    ssid = ssid,
    bssid = bssid,
    rssiDbm = rssiDbm,
    linkSpeedMbps = linkSpeedMbps,
    frequencyMhz = frequencyMhz
)

fun WifiInfo.toEntity(): WifiEntity = WifiEntity(
    timestamp = timestamp,
    ssid = ssid,
    bssid = bssid,
    rssiDbm = rssiDbm,
    linkSpeedMbps = linkSpeedMbps,
    frequencyMhz = frequencyMhz
)
