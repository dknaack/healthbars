package com.dknaack.healthbars.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.Period

@Database(entities = [HealthBar::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthBarDao(): HealthBarDao
}

class Converters {
    @TypeConverter
    fun localDateFromString(value: String?): LocalDate? {
        return LocalDate.parse(value)
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun periodFromString(value: String?): Period? {
        return Period.parse(value)
    }

    @TypeConverter
    fun periodToString(period: Period?): String? {
        return period.toString()
    }
}