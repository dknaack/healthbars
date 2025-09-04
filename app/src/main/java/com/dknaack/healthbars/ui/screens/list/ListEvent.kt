package com.dknaack.healthbars.ui.screens.list

import androidx.datastore.preferences.protobuf.StringValue
import com.dknaack.healthbars.data.HealthBar

sealed interface ListEvent {
    data class Sort(val sortType: SortType): ListEvent
    data object BeginSearch: ListEvent
    data class SetQuery(val query: String): ListEvent
    data object EndSearch: ListEvent
}