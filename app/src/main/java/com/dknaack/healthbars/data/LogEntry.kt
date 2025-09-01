package com.dknaack.healthbars.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "health_bar_id") val healthBarId: Long,
    val action: LogAction,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

enum class LogAction {
    CREATE,
    DEATH,
    REFRESH,
    SKIP,
}
