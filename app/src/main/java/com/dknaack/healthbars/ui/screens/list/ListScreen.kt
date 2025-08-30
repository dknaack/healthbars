package com.dknaack.healthbars.ui.screens.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dknaack.healthbars.NavItem
import com.dknaack.healthbars.components.HealthBarIndicator
import com.dknaack.healthbars.data.HealthBar
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Shows all created health bars.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ListScreen(
    navController: NavController,
    healthBars: List<HealthBar>,
    onDelete: (HealthBar) -> Unit,
    onClick: (HealthBar) -> Unit,
    modifier: Modifier = Modifier,
) {
    var contextHealthBarId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Health Bars") },
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        ) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(NavItem.UpsertScreen())
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
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
                    onDelete = onDelete,
                    onEdit = { contextHealthBarId = healthBar.id },
                    onClick = onClick,
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
    val remainingPeriodText = if (remainingPeriod.years > 1) {
        "${remainingPeriod.years} years left"
    } else if (remainingPeriod.years > 0) {
        "1 year left"
    } else if (remainingPeriod.months > 1) {
        "${remainingPeriod.months} months left"
    } else if (remainingPeriod.months > 0) {
        "1 month left"
    } else if (remainingPeriod.days >= 14) {
        "${remainingPeriod.days / 7} weeks left"
    } else if (remainingPeriod.days >= 7) {
        "1 week left"
    } else if (remainingPeriod.days > 1) {
        "${remainingPeriod.days} days left"
    } else if (remainingPeriod.days > 0) {
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