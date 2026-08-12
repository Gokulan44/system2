package com.systemmonitor.features.remotepermission.data.dao

import androidx.room.*
import com.systemmonitor.features.remotepermission.data.entity.ResourceRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceRequestDao {
    @Query("SELECT * FROM resource_requests")
    fun getAllResourcesFlow(): Flow<List<ResourceRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceRequestEntity)

    @Query("SELECT * FROM resource_requests WHERE resourceId = :id LIMIT 1")
    suspend fun getResourceById(id: String): ResourceRequestEntity?
}
