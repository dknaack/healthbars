package com.dknaack.healthbars.ui.screens.overview

sealed interface OverviewEvent {
    data class Show(val healthBarId: Long): OverviewEvent
    data object Refresh: OverviewEvent
    data object Skip: OverviewEvent
}