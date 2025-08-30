package com.dknaack.healthbars.ui.screens.list

import com.dknaack.healthbars.data.HealthBar

sealed interface ListEvent {
    object CreateHealthBar: ListEvent
    data class ViewHealthBar(val id: Long): ListEvent
    data class DeleteHealthBar(val id: Long): ListEvent
    data class UpsertHealthBar(val healthBar: HealthBar): ListEvent
}