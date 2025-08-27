package com.dknaack.healthbars

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Bundle
import android.widget.NumberPicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.experimental.Experimental
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dknaack.healthbars.ui.theme.HealthBarsTheme
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Testing testing 123")

        setContent {
            var navController = rememberNavController()

            var healthBars: SnapshotStateList<HealthBar> = remember {
                mutableStateListOf(
                    HealthBar(
                        name = "Foo",
                        duration = Period.ofDays(10),
                        startDate = LocalDate.now().minusDays(1),
                    ),
                    HealthBar(
                        name = "Bar",
                        duration = Period.ofDays(8),
                        startDate = LocalDate.now().minusDays(2),
                    ),
                    HealthBar(
                        name = "Baz",
                        duration = Period.ofDays(5),
                        startDate = LocalDate.now().minusDays(3),
                    ),
                    HealthBar(
                        name = "Example",
                        duration = Period.ofDays(10),
                        startDate = LocalDate.now().minusDays(4),
                    ),
                )
            }

            NavHost(
                navController = navController,
                startDestination = "healthBarList",
            ) {
                composable("healthBarList") {
                    HealthBarListScreen(healthBars, navController)
                }
                composable("createHealthBar") {
                    CreateHealthBarScreen(
                        navController,
                        onCreateHealthBar = { healthBars += it },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthBarListScreen(
    healthBars: SnapshotStateList<HealthBar>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Health Bars")
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("createHealthBar")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { innerPadding ->
        LazyColumn(Modifier.padding(innerPadding)) {
            items(
                items = healthBars,
                key = { it.name },
            ) { healthBar ->
                HealthBarCard(
                    healthBar = healthBar,
                    onRemove = { healthBars -= healthBar },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

data class HealthBar(
    val name: String,
    val duration: Period,
    val startDate: LocalDate,
)

@Composable
fun HealthBarCard(
    healthBar: HealthBar,
    onRemove: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Calculate the progress
    val currentDate = LocalDate.now()
    val endDate = healthBar.startDate.plus(healthBar.duration)
    val elapsed = ChronoUnit.DAYS.between(healthBar.startDate, currentDate)
    val duration = ChronoUnit.DAYS.between(healthBar.startDate, endDate)
    val progress = max(0f, min(1f, elapsed.toFloat() / duration.toFloat()))

    // Format the starting date
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val startDateText = healthBar.startDate.format(dateFormatter)

    // Format the remaining period
    val remainingPeriod = Period.between(currentDate, endDate)
    val remainingPeriodText = if (remainingPeriod.years > 1) {
        "${remainingPeriod.years} years left"
    } else if (remainingPeriod.years == 1) {
        "1 year left"
    } else if (remainingPeriod.months > 1) {
        "${remainingPeriod.months} months left"
    } else if (remainingPeriod.months == 1) {
        "1 month left"
    } else if (remainingPeriod.days > 1) {
        "${remainingPeriod.days} days left"
    } else if (remainingPeriod.days == 1) {
        "1 day left"
    } else {
        ""
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                println("Edit")
            } else if (it == SwipeToDismissBoxValue.EndToStart) {
                onRemove(healthBar)
            }

            it != SwipeToDismissBoxValue.StartToEnd
        }
    )

    SwipeToDismissBox (
        state = swipeToDismissBoxState,
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue)
                        .wrapContentSize(Alignment.CenterStart)
                        .padding(12.dp),
                    tint = Color.White,
                )
                SwipeToDismissBoxValue.EndToStart -> Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                        .wrapContentSize(Alignment.CenterEnd)
                        .padding(12.dp),
                    tint = Color.White,
                )
                SwipeToDismissBoxValue.Settled -> {}
            }
        }
    ) {
        AnimatedVisibility(
            visible = swipeToDismissBoxState.dismissDirection != SwipeToDismissBoxValue.EndToStart,
            enter = slideInHorizontally(),
            exit = slideOutHorizontally(),
        ) {
            ListItem(
                headlineContent = { Text(healthBar.name) },
                supportingContent = {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(16.dp),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(startDateText)
                            Text(remainingPeriodText)
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHealthBarScreen(
    navController: NavController,
    onCreateHealthBar: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("0") }
    var months by remember { mutableStateOf("0") }
    var years by remember { mutableStateOf("0") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create New Health Bar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onCreateHealthBar(HealthBar(
                    name = name,
                    duration = Period.of(
                        years.toInt(),
                        months.toInt(),
                        days.toInt()
                    ),
                    startDate = LocalDate.now())
                )
                navController.navigate("healthBarList")
            }) {
                Icon(Icons.Default.Check, contentDescription = "Create")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = years,
                    onValueChange = { years = it },
                    label = { Text("Years") },
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = months,
                    onValueChange = { months = it },
                    label = { Text("Months") },
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Days") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}