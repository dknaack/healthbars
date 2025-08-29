package com.dknaack.healthbars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class MainActivity : ComponentActivity() {
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
                    MainScreen(
                        healthBars = healthBars.value,
                        onViewHealthBar = {
                            selectedHealthBar = it
                            navController.navigate("viewHealthBar")
                        },
                        onDeleteHealthBar = {
                            scope.launch { healthBarDao.delete(it) }
                        },
                        navController)
                }
                composable("createHealthBar") {
                    CreateScreen(
                        navController,
                        onCreateHealthBar = {
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

@Entity(tableName = "health_bars")
data class HealthBar(
    @PrimaryKey val id: Long,
    val name: String,
    val duration: Period,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
)

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

fun HealthBar.getProgress(): Float {
    val endDate = startDate.plus(duration)
    val currentDate = LocalDate.now()
    val elapsed = currentDate.toEpochDay() - startDate.toEpochDay()
    val total = endDate.toEpochDay() - startDate.toEpochDay()
    val progress = 1f - elapsed.toFloat() / total
    return progress
}

@Dao
interface HealthBarDao {
    @Insert
    suspend fun insert(healthBar: HealthBar)

    @Delete
    suspend fun delete(healthBar: HealthBar)

    @Query("SELECT * FROM health_bars")
    fun getAll(): Flow<List<HealthBar>>
}

@Database(entities = [HealthBar::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthBarDao(): HealthBarDao
}

/**
 * A button with an icon and a label with the correct background suitable for a bottom sheet.
 */
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

/**
 * Shows all created health bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    healthBars: List<HealthBar>,
    onViewHealthBar: (HealthBar) -> Unit,
    onDeleteHealthBar: (HealthBar) -> Unit,
    navController: NavController,
) {
    var contextHealthBarId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Bars") },
                actions = {
                    IconButton(onClick = { println("Settings") }) {
                        Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("createHealthBar")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                    onDelete = onDeleteHealthBar,
                    onEdit = { contextHealthBarId = healthBar.id },
                    onClick = onViewHealthBar,
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

@Composable
fun HealthBarIndicator(healthBar: HealthBar) {
    LinearProgressIndicator(
        drawStopIndicator = { },
        progress = { healthBar.getProgress() },
        modifier = Modifier.fillMaxWidth().height(16.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthBarCard(
    healthBar: HealthBar,
    onDelete: (HealthBar) -> Unit,
    onEdit: (HealthBar) -> Unit,
    onClick: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Format the starting date
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val startDateText = dateFormatter.format(healthBar.startDate)

    // Format the remaining period
    val currentDate = LocalDate.now()
    val endDate = healthBar.startDate.plus(healthBar.duration)
    val remainingPeriod = Period.between(currentDate, endDate)
    val years = remainingPeriod.years
    val months = remainingPeriod.months
    val days = remainingPeriod.days
    val remainingPeriodText = if (years > 1) {
        "$years years left"
    } else if (years > 0) {
        "1 year left"
    } else if (months > 1) {
        "$months months left"
    } else if (months > 0) {
        "1 month left"
    } else if (days >= 14) {
        "${days/7} weeks left"
    } else if (days >= 7) {
        "1 week left"
    } else if (days > 1) {
        "$days days left"
    } else if (days > 0) {
        "1 day left"
    } else {
        ""
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                println("Edit")
            } else if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(healthBar)
            }

            it != SwipeToDismissBoxValue.StartToEnd
        }
    )

    val haptics = LocalHapticFeedback.current

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = {
                    onClick(healthBar)
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit(healthBar)
                },
                onLongClickLabel = "Edit",
            ),
            headlineContent = {
                Text(
                    healthBar.name,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            supportingContent = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HealthBarIndicator(healthBar)
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

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onClick: () -> Unit = { },
) {
    // Name
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .padding(start = 16.dp)
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                readOnly = readOnly,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )
        }

        if (readOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        role = Role.Button,
                    ) { onClick() }
            )
        }
    }

    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    navController: NavController,
    onCreateHealthBar: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val startDate = datePickerState.selectedDateMillis?.let {
        val instant = Instant.ofEpochMilli(it)
        val zoneId = ZoneId.systemDefault()
        instant.atZone(zoneId).toLocalDate()
    } ?: LocalDate.now()

    var expanded by remember { mutableStateOf(false) }
    var periodUnit by remember { mutableStateOf(ChronoUnit.DAYS) }
    var periodValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Health Bar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Discard")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val endDate = startDate.plus(periodValue.toLong(), periodUnit)

                        onCreateHealthBar(HealthBar(
                            id = System.currentTimeMillis(),
                            name = name,
                            startDate = startDate,
                            duration = Period.between(startDate, endDate)
                        ))

                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.Outlined.Save, contentDescription = "Discard")
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
            )

            // Date Field
            Text("Start Date")
            OutlinedTextField(
                value = dateFormatter.format(startDate),
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = !showDatePicker }) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Date")
                    }
                },
            )

            // Period Field
            Text("Duration")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = periodValue,
                    onValueChange = { periodValue = it },
                    modifier = Modifier.fillMaxWidth(.5f),
                )

                Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                    OutlinedTextField(
                        value = periodUnit.toString(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
                            Icon(icon, "")
                        },
                        onValueChange = { },
                        readOnly = true,
                    )

                    // Transparent clickable surface on top of OutlinedTextField
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable { expanded = true },
                        color = Color.Transparent,
                    ) {}

                    fun selectUnit(unit: ChronoUnit) {
                        periodUnit = unit
                        expanded = false
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Days") },
                            onClick = { selectUnit(ChronoUnit.DAYS) },
                        )
                        DropdownMenuItem(
                            text = { Text("Weeks") },
                            onClick = { selectUnit(ChronoUnit.WEEKS) },
                        )
                        DropdownMenuItem(
                            text = { Text("Months") },
                            onClick = { selectUnit(ChronoUnit.MONTHS) },
                        )
                        DropdownMenuItem(
                            text = { Text("Years") },
                            onClick = { selectUnit(ChronoUnit.YEARS) },
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
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

/**
 * Shows data for a health bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    navController: NavController,
    healthBar: HealthBar,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(healthBar.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { println("Edit") }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 30.sp)) {
                                    append(round(healthBar.getProgress() * 100f).toInt().toString())
                                }
                                withStyle(SpanStyle(fontSize = 14.sp)) {
                                    append("%")
                                }
                            },
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    HealthBarIndicator(healthBar)

                    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    val endDate = healthBar.startDate.plus(healthBar.duration)
                    Text("Your ${healthBar.name} dies on ${dateFormatter.format(endDate)}")
                }
            }
        }
    }
}