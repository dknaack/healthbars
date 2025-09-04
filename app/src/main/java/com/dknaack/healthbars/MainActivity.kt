package com.dknaack.healthbars

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import com.dknaack.healthbars.data.AppDatabase
import com.dknaack.healthbars.ui.screens.list.ListScreen
import com.dknaack.healthbars.ui.screens.list.ListViewModel
import com.dknaack.healthbars.ui.screens.overview.OverviewEvent
import com.dknaack.healthbars.ui.screens.overview.OverviewScreen
import com.dknaack.healthbars.ui.screens.overview.OverviewViewModel
import com.dknaack.healthbars.ui.screens.upsert.UpsertEvent
import com.dknaack.healthbars.ui.screens.upsert.UpsertScreen
import com.dknaack.healthbars.ui.screens.upsert.UpsertViewModel
import com.dknaack.healthbars.ui.theme.HealthBarsTheme
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.jar.Manifest

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app.db"
        ).build()
    }

    private val listViewModel by viewModels<ListViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListViewModel(db.healthBarDao()) as T
                }
            }
        }
    )

    private val upsertViewModel by viewModels<UpsertViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UpsertViewModel(
                        db.healthBarDao(),
                    ) as T
                }
            }
        }
    )

    private val overviewViewModel by viewModels<OverviewViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OverviewViewModel(
                        healthBarDao = db.healthBarDao(),
                        logDao = db.logDao(),
                    ) as T
                }
            }
        }
    )

    companion object {
        const val CHANNEL_ID = "health_bars"
        const val NOTIFICATION_ID = 1
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.appcompat.R.drawable.abc_vector_test)
            .setContentTitle("Test notification")
            .setContentText("This is a test notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }

            println("Sending notification!!")
            notify(notificationId, builder.build())
        }
    }

    fun registerAlarm() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", "Hello, world!")
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            LocalDateTime.now()
                .plusSeconds(10)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond() * 1000,
            PendingIntent.getBroadcast(
                this,
                "Hello".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        registerAlarm()

        setContent {
            HealthBarsTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavItem.ListScreen,
                    ) {
                        composable<NavItem.ListScreen> {
                            val state by listViewModel.state.collectAsState()
                            ListScreen(
                                state = state,
                                onEvent = listViewModel::onEvent,
                                navController = navController,
                            )
                        }
                        composable<NavItem.UpsertScreen> {
                            val args: NavItem.UpsertScreen = it.toRoute()
                            val state by upsertViewModel.state.collectAsState()
                            LaunchedEffect(args.id) {
                                if (args.id != null) {
                                    upsertViewModel.onEvent(UpsertEvent.SetId(args.id))
                                } else {
                                    upsertViewModel.onEvent(UpsertEvent.SetId(0))
                                }
                            }

                            UpsertScreen(
                                state = state,
                                onEvent = upsertViewModel::onEvent,
                                navController = navController,
                                modifier = Modifier.animateEnterExit(
                                    enter = slideInVertically(),
                                    exit = slideOutVertically(),
                                )
                            )
                        }
                        composable<NavItem.ViewScreen> {
                            val args: NavItem.ViewScreen = it.toRoute()
                            overviewViewModel.onEvent(OverviewEvent.Show(args.id))

                            val state by overviewViewModel.state.collectAsState()
                            OverviewScreen(
                                navController = navController,
                                state = state,
                                onEvent = overviewViewModel::onEvent,
                                modifier = Modifier.animateEnterExit(
                                    enter = slideInVertically(),
                                    exit = slideOutVertically(),
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Serializable
sealed class NavItem {
    @Serializable
    object ListScreen : NavItem()
    @Serializable
    data class ViewScreen(val id: Long) : NavItem()
    @Serializable
    data class UpsertScreen(val id: Long? = null) : NavItem()
}
