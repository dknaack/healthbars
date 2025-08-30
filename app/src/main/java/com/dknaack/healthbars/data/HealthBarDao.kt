package com.dknaack.healthbars.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthBarDao {
    @Upsert
    suspend fun upsert(healthBar: HealthBar)

    @Delete
    suspend fun delete(healthBar: HealthBar)

    @Query("SELECT * FROM health_bars WHERE id = :id")
    suspend fun get(id: Long): HealthBar?

    @Query("SELECT * FROM health_bars")
    fun getAll(): Flow<List<HealthBar>>
}
