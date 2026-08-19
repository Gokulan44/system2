package com.systemmonitor.securityscan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.securityscan.database.entity.KnownHashEntity

@Dao
interface KnownHashDao {
    @Query("SELECT * FROM security_known_hashes WHERE sha256 = :sha256 LIMIT 1")
    suspend fun getKnownHash(sha256: String): KnownHashEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnownHashes(hashes: List<KnownHashEntity>)

    @Query("DELETE FROM security_known_hashes")
    suspend fun clearHashes()
}