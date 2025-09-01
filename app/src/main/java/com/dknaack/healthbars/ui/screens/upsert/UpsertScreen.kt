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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertScreen(
    navController: NavController,
    state: UpsertState,
    onEvent: (UpsertEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    if (state.id == 0L)
                        "New"
                    else
                        "Edit"
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isDirty) {
                            showDiscardDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Discard")
                    }

                    if (showDiscardDialog) {
                        AlertDialog(
                            onDismissRequest = { showDiscardDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    onEvent(UpsertEvent.Save)
                                    navController.popBackStack()
                                    showDiscardDialog = false
                                }) {
                                    Text("Keep editing")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    navController.popBackStack()
                                    showDiscardDialog = false
                                }) {
                                    Text("Discard")
                                }
                            },
                            title = {
                                Text("Discard changes")
                            },
                            text = {
                                Text("Are you sure you want to discard your changes?")
                            },
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEvent(UpsertEvent.Save)
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
                value = state.name,
                onValueChange = { onEvent(UpsertEvent.SetName(it)) },
                modifier = Modifier.fillMaxWidth(),
            )

            // Date Field
            Text("Start Date")
            OutlinedTextField(
                value = state.startDate,
                onValueChange = { onEvent(UpsertEvent.SetStartDate(it)) },
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
                    value = state.duration,
                    onValueChange = { onEvent(UpsertEvent.SetDuration(it)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    modifier = Modifier.fillMaxWidth(.5f),
                )

                Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                    OutlinedTextField(
                        value = state.durationUnit.toString(),
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
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable { expanded = true },
                        color = Color.Transparent,
                    ) {}

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Days") },
                            onClick = {
                                onEvent(UpsertEvent.SetDurationUnit(ChronoUnit.DAYS))
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Weeks") },
                            onClick = {
                                onEvent(UpsertEvent.SetDurationUnit(ChronoUnit.WEEKS))
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Months") },
                            onClick = {
                                onEvent(UpsertEvent.SetDurationUnit(ChronoUnit.MONTHS))
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Years") },
                            onClick = {
                                onEvent(UpsertEvent.SetDurationUnit(ChronoUnit.YEARS))
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        onEvent(UpsertEvent.SetStartDateMillis(datePickerState.selectedDateMillis))
                        showDatePicker = false
                    }) {
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
