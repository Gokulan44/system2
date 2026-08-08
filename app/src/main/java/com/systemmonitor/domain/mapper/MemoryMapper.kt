package com.systemmonitor.domain.mapper

import com.systemmonitor.domain.model.Memory
import com.systemmonitor.local.database.entity.MemoryEntity

fun MemoryEntity.toDomain(): Memory = Memory(
    timestamp = timestamp,
    totalMb = totalMb,
    availableMb = availableMb,
    usedMb = usedMb,
    thresholdMb = thresholdMb,
    isLowMemory = isLowMemory
)

fun Memory.toEntity(): MemoryEntity = MemoryEntity(
    timestamp = timestamp,
    totalMb = totalMb,
    availableMb = availableMb,
    usedMb = usedMb,
    thresholdMb = thresholdMb,
    isLowMemory = isLowMemory
)
