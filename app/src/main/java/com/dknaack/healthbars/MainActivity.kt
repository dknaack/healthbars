package com.dknaack.healthbars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.dknaack.healthbars.ui.theme.HealthBarsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app.db"
        ).build()
    }

    private val viewModel by viewModels<EventViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventViewModel(db.healthBarDao()) as T
                }
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).build()

        val healthBarDao = db.healthBarDao()

        setContent {
            val navController = rememberNavController()
            val healthBarsFlow = remember { healthBarDao.getAll() }
            val healthBars = healthBarsFlow.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            var selectedHealthBar by remember { mutableStateOf<HealthBar?>(null) }

            NavHost(
                navController = navController,
                startDestination = "healthBarList",
            ) {
                composable("healthBarList") {
                    ListScreen(
                        navController = navController,
                        healthBars = healthBars.value,
                        onClick = {
                            selectedHealthBar = it
                            navController.navigate("viewHealthBar")
                        },
                        onDelete = {
                            scope.launch { healthBarDao.delete(it) }
                        },
                    )
                }
                composable("createHealthBar") {
                    UpsertScreen(
                        navController,
                        onUpsert = {
                            scope.launch { healthBarDao.insert(it) }
                        },
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(),
                            exit = slideOutVertically(),
                        )
                    )
                }
                composable("viewHealthBar") {
                    if (selectedHealthBar != null) {
                        ViewScreen(
                            navController,
                            selectedHealthBar!!,
                        )
                    } else {
                        navController.navigate("healthBarList")
                    }
                }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun localDateFromEpochDays(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun localDateToEpochDays(date: LocalDate?): Long? {
        return date?.toEpochDay()
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

@Database(entities = [HealthBar::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthBarDao(): HealthBarDao
}