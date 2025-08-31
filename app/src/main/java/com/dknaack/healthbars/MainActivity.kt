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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.dknaack.healthbars.data.AppDatabase
import com.dknaack.healthbars.data.HealthBar
import com.dknaack.healthbars.ui.screens.list.ListScreen
import com.dknaack.healthbars.ui.screens.list.ListViewModel
import com.dknaack.healthbars.ui.screens.overview.ViewScreen
import com.dknaack.healthbars.ui.screens.upsert.UpsertScreen
import com.dknaack.healthbars.ui.theme.HealthBarsTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app.db"
        ).build()
    }

    private val viewModel by viewModels<ListViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListViewModel(db.healthBarDao()) as T
                }
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HealthBarsTheme {
                val navController = rememberNavController()
                var selectedHealthBar by remember { mutableStateOf<HealthBar?>(null) }

                NavHost(
                    navController = navController,
                    startDestination = NavItem.ListScreen,
                ) {
                    composable<NavItem.ListScreen> {
                        val state by viewModel.state.collectAsState()
                        ListScreen(
                            state = state,
                            onEvent = viewModel::onEvent,
                        )
                    }
                    composable<NavItem.UpsertScreen> {
                        UpsertScreen(
                            navController,
                            onUpsert = { },
                            modifier = Modifier.animateEnterExit(
                                enter = slideInVertically(),
                                exit = slideOutVertically(),
                            )
                        )
                    }
                    composable<NavItem.ViewScreen> {
                        if (selectedHealthBar != null) {
                            ViewScreen(
                                navController,
                                selectedHealthBar!!,
                            )
                        } else {
                            navController.popBackStack()
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
