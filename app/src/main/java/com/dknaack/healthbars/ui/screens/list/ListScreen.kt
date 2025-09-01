package com.dknaack.healthbars.ui.screens.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    state: ListUiState,
    onEvent: (ListEvent) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Health Bars") },
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { isSortMenuExpanded = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }

                fun onClick(sortType: SortType) {
                    onEvent(ListEvent.Sort(sortType))
                    isSortMenuExpanded = false
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Name") },
                        trailingIcon = {
                            val selected = (state.sortType == SortType.NAME)
                            RadioButton(selected = selected, onClick = { onClick(SortType.NAME) })
                        },
                        onClick = { onClick(SortType.NAME) },
                    )
                    DropdownMenuItem(
                        text = { Text("End Date") },
                        trailingIcon = {
                            val selected = (state.sortType == SortType.END_DATE)
                            RadioButton(selected = selected, onClick = { onClick(SortType.END_DATE) })
                        },
                        onClick = { onClick(SortType.END_DATE) },
                    )
                }
            }
        ) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(NavItem.UpsertScreen(null))
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
            items(state.healthBars) { healthBar ->
                HealthBarCard(
                    healthBar = healthBar,
                    onClick = {
                        navController.navigate(NavItem.ViewScreen(healthBar.id))
                    },
                    modifier = Modifier.animateItem(),
                )
            }

            item {
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthBarCard(
    healthBar: HealthBar,
    onClick: () -> Unit,
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

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier,
    ) {
        ListItem(
            modifier = Modifier.clickable { onClick() },
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