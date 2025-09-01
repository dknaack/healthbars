package com.dknaack.healthbars.ui.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dknaack.healthbars.NavItem
import com.dknaack.healthbars.components.HealthBarIndicator
import com.dknaack.healthbars.data.HealthBar
import com.dknaack.healthbars.data.LogAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.round

/**
 * Shows data for a health bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    navController: NavController,
    state: OverviewState,
    onEvent: (OverviewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.healthBar == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val endDate = state.healthBar.startDate.plus(state.healthBar.duration)
        val endDateText = dateFormatter.format(endDate)

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(state.healthBar.name) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(NavItem.UpsertScreen(
                                id = state.healthBar.id,
                            ))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(MaterialTheme.typography.displayLarge.toSpanStyle()) {
                                            val progress = state.healthBar.getProgress()
                                            append(round(progress * 100f).toInt().toString())
                                        }
                                        withStyle(SpanStyle(fontSize = 14.sp)) {
                                            append("%")
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            HealthBarIndicator(state.healthBar)

                            Text("Until $endDateText")

                            Spacer(Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(64.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { onEvent(OverviewEvent.Refresh) },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    enabled = LocalDate.now().isAfter(state.healthBar.startDate)
                                ) {
                                    Icon(Icons.Default.Refresh, "Refresh")
                                    Spacer(Modifier.width(4.dp))
                                    Text("Refresh")
                                }
                                FilledTonalButton(
                                    onClick = { onEvent(OverviewEvent.Skip) },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                ) {
                                    Icon(Icons.Default.SkipNext, "Skip")
                                    Spacer(Modifier.width(4.dp))
                                    Text("Skip")
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                }

                items(state.logEntries) { logEntry ->
                    val (icon, contentDescription) = when (logEntry.action) {
                        LogAction.SKIP    -> Pair(Icons.Default.SkipNext, "Skip")
                        LogAction.CREATE  -> Pair(Icons.Default.Add, "Add")
                        LogAction.REFRESH -> Pair(Icons.Default.Refresh, "Refresh")
                        LogAction.DEATH   -> Pair(Icons.Default.Warning, "Death")
                    }

                    ListItem(
                        leadingContent = {
                            Icon(icon, contentDescription)
                        },
                        headlineContent = {
                            Text(contentDescription)
                        },
                        supportingContent = {
                            Text(dateFormatter.format(logEntry.timestamp))
                        }
                    )
                }
            }
        }
    }
}
