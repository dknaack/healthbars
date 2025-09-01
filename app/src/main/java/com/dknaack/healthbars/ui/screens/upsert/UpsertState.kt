package com.dknaack.healthbars.ui.screens.upsert

import java.time.temporal.ChronoUnit

data class UpsertState(
    val id: Long = 0,
    val name: String = "",
    val startDate: String = "",
    val duration: String = "",
    val durationUnit: ChronoUnit = ChronoUnit.DAYS,
    val isDirty: Boolean = false,
)