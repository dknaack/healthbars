package com.dknaack.healthbars.ui.screens.upsert

import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface UpsertEvent {
    data class SetId(val id: Long): UpsertEvent
    data class SetName(val name: String): UpsertEvent
    data class SetDuration(val duration: String): UpsertEvent
    data class SetDurationUnit(val unit: ChronoUnit): UpsertEvent
    data class SetStartDate(val startDate: String): UpsertEvent
    data class SetStartDateMillis(val value: Long?): UpsertEvent
    data object Discard: UpsertEvent
    data object Save: UpsertEvent
}