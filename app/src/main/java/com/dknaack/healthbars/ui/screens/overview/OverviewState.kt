package com.dknaack.healthbars.ui.screens.overview

import com.dknaack.healthbars.data.HealthBar
import com.dknaack.healthbars.data.LogEntry
import java.time.LocalDate

data class OverviewState(
    val logEntries: List<LogEntry> = emptyList(),
    val healthBar: HealthBar? = null,
)
