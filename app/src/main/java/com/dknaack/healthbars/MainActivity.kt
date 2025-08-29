package com.dknaack.healthbars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Testing testing 123")

        setContent {
            val navController = rememberNavController()

            val healthBars: SnapshotStateList<HealthBar> = remember {
                mutableStateListOf(
                    HealthBar(
                        id = 1,
                        name = "Foo",
                        duration = Period.ofDays(10),
                        startDate = LocalDate.now().minusDays(1),
                    ),
                    HealthBar(
                        id = 2,
                        name = "Bar",
                        duration = Period.ofDays(8),
                        startDate = LocalDate.now().minusDays(2),
                    ),
                    HealthBar(
                        id = 3,
                        name = "Baz",
                        duration = Period.ofDays(5),
                        startDate = LocalDate.now().minusDays(3),
                    ),
                    HealthBar(
                        id = 4,
                        name = "Example",
                        duration = Period.ofDays(10),
                        startDate = LocalDate.now().minusDays(4),
                    ),
                    HealthBar(
                        id = 5,
                        name = "Example",
                        duration = Period.ofDays(10),
                        startDate = LocalDate.now().minusDays(4),
                    ),
                    HealthBar(
                        id = 6,
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

@Composable
fun BottomSheetButton(
    imageVector: ImageVector,
    contentDescription: String,
    text: String,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = { Icon(imageVector, contentDescription) },
        headlineContent = { Text(text) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthBarListScreen(
    healthBars: SnapshotStateList<HealthBar>,
    navController: NavController,
) {
    var contextHealthBarId by rememberSaveable { mutableStateOf<Long?>(null) }

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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(
                items = healthBars,
                key = { it.id },
            ) { healthBar ->
                HealthBarCard(
                    healthBar = healthBar,
                    onRemove = { healthBars -= healthBar },
                    onEdit = { contextHealthBarId = healthBar.id },
                    modifier = Modifier.animateItem(),
                )
            }

            item {
                Spacer(Modifier.height(64.dp))
            }
        }

        if (contextHealthBarId != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    contextHealthBarId = null
                }
            ) {
                Column {
                    BottomSheetButton(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        text = "Edit",
                        onClick = { println("Edit") }
                    )
                    BottomSheetButton(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        text = "Refresh",
                        onClick = { println("Refresh") }
                    )
                    BottomSheetButton(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        text = "Delete",
                        onClick = { println("Delete") }
                    )
                }
            }
        }
    }
}

data class HealthBar(
    val id: Long,
    val name: String,
    val duration: Period,
    val startDate: LocalDate,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthBarCard(
    healthBar: HealthBar,
    onRemove: (HealthBar) -> Unit,
    onEdit: (HealthBar) -> Unit,
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
    } else if (remainingPeriod.days >= 14) {
        "${remainingPeriod.days / 7} weeks left"
    } else if (remainingPeriod.days >= 7) {
        "1 week left"
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

    val haptics = LocalHapticFeedback.current

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
                modifier = Modifier.combinedClickable(
                    onClick = {
                        println("Clicked ${healthBar.id}")
                    },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit(healthBar)
                    },
                    onLongClickLabel = "Edit",
                ),
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

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val startDate = datePickerState.selectedDateMillis?.let {
        val instant = Instant.ofEpochMilli(it)
        val zoneId = ZoneId.systemDefault()
        instant.atZone(zoneId).toLocalDate()
    } ?: LocalDate.now()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create New Health Bar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onCreateHealthBar(HealthBar(
                    id = System.currentTimeMillis(),
                    name = name,
                    duration = Period.of(
                        years.toInt(),
                        months.toInt(),
                        days.toInt()
                    ),
                    startDate = startDate)
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
            // Name Input
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Start Date Input
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = dateFormatter.format(startDate),
                    onValueChange = { },
                    label = { Text("Starting Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = !showDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date",
                            )
                        }
                    }
                )
            }

            // Period Input
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

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { },
                    confirmButton = {
                        TextButton(onClick = {
                            println("Confirmed!")
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            println("dismissed")
                            showDatePicker = false
                        }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}