package com.dknaack.healthbars.ui.screens.list

import com.dknaack.healthbars.data.HealthBar

data class ListUiState(
    val healthBars: List<HealthBar> = emptyList(),
    val sortType: SortType = SortType.NAME,
)

enum class SortType {
    NAME,
    END_DATE,
}
