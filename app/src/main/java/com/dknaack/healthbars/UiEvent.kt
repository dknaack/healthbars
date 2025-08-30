package com.dknaack.healthbars

import com.dknaack.healthbars.data.HealthBar

sealed interface UiEvent {
    object CreateHealthBar: UiEvent
    data class ViewHealthBar(val id: Long): UiEvent
    data class DeleteHealthBar(val id: Long): UiEvent
    data class UpsertHealthBar(val healthBar: HealthBar): UiEvent
}