package com.dknaack.healthbars.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogEntry)

    @Query("DELETE FROM log_entries WHERE health_bar_id = :healthBarId")
    suspend fun deleteLogsForHealthBar(healthBarId: Long)

    @Query("SELECT * FROM log_entries WHERE health_bar_id = :healthBarId")
    fun getLog(healthBarId: Long): Flow<List<LogEntry>>
}