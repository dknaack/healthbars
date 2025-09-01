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
    fun get(id: Long): Flow<HealthBar?>

    @Query("SELECT * FROM health_bars ORDER BY name")
    fun getAll(): Flow<List<HealthBar>>

    @Query("SELECT * FROM health_bars ORDER BY end_date ASC")
    fun getAllOrderedByEndDate(): Flow<List<HealthBar>>
}
