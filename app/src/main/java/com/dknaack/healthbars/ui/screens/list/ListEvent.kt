package com.dknaack.healthbars.ui.screens.list

import com.dknaack.healthbars.data.HealthBar

sealed interface ListEvent {
    data class Sort(val sortType: SortType): ListEvent
}