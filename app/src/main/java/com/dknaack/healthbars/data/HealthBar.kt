package com.dknaack.healthbars.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period
import kotlin.math.max
import kotlin.math.min

@Entity(tableName = "health_bars")
data class HealthBar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate,
) {
    val duration: Period
        get() = Period.between(startDate, endDate)

    fun getProgress(): Float {
        val currentDate = LocalDate.now()
        val elapsed = currentDate.toEpochDay() - startDate.toEpochDay()
        val total = endDate.toEpochDay() - startDate.toEpochDay()
        val progress = 1f - elapsed.toFloat() / total
        return max(0f, min(1f, progress))
    }
}