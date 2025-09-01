package com.dknaack.healthbars.ui.screens.list

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dknaack.healthbars.data.HealthBarDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(
    private val dao: HealthBarDao,
): ViewModel() {
    private val _sortType = MutableStateFlow(SortType.NAME)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _healthBars = _sortType
        .flatMapLatest { sortType ->
            when (sortType) {
                SortType.NAME -> dao.getAll()
                SortType.END_DATE -> dao.getAllOrderedByEndDate()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    private val _state = MutableStateFlow(ListUiState())
    val state = combine(_state, _sortType, _healthBars) { state, sortType, healthBars ->
        state.copy(
            healthBars = healthBars,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.Sort -> {
                _sortType.value = event.sortType
            }
        }
    }
}