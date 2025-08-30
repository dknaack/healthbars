package com.dknaack.healthbars.ui.screens.upsert

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dknaack.healthbars.data.HealthBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertScreen(
    navController: NavController,
    onUpsert: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
    healthBar: HealthBar? = null,
) {
    var name by remember { mutableStateOf(healthBar?.name ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val startDate = healthBar?.startDate ?: datePickerState.selectedDateMillis?.let {
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
                title = { Text(if (healthBar != null) "Edit" else "New") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Discard")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val endDate = startDate.plus(periodValue.toLong(), periodUnit)

                        onUpsert(
                            HealthBar(
                                id = healthBar?.id ?: System.currentTimeMillis(),
                                name = name,
                                startDate = startDate,
                                endDate = endDate,
                            )
                        )

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
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
