package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemmonitor.local.database.entity.LaptopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaptopDao {

    @Query("SELECT * FROM laptops ORDER BY lastSeen DESC")
    fun getAllLaptops(): Flow<List<LaptopEntity>>

    @Query("SELECT * FROM laptops WHERE id = :id LIMIT 1")
    suspend fun getLaptopById(id: String): LaptopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaptop(laptop: LaptopEntity)

    @Update
    suspend fun updateLaptop(laptop: LaptopEntity)

    @Delete
    suspend fun deleteLaptop(laptop: LaptopEntity)

    @Query("DELETE FROM laptops WHERE id = :id")
    suspend fun deleteLaptopById(id: String)
}
