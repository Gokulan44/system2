package com.systemmonitor.domain.mapper

import com.systemmonitor.domain.model.Storage
import com.systemmonitor.local.database.entity.StorageEntity

fun StorageEntity.toDomain(): Storage = Storage(
    timestamp = timestamp,
    totalBytes = totalBytes,
    freeBytes = freeBytes,
    usedBytes = usedBytes
)

fun Storage.toEntity(): StorageEntity = StorageEntity(
    timestamp = timestamp,
    totalBytes = totalBytes,
    freeBytes = freeBytes,
    usedBytes = usedBytes
)
