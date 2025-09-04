package com.dknaack.healthbars.ui.screens.list

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

class ListViewModel(
    private val dao: HealthBarDao,
): ViewModel() {
    private val _state = MutableStateFlow(ListUiState())
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _healthBars = _state
        .flatMapLatest { state ->
            if (state.isSearching) {
                dao.findByName(state.query)
            } else {
                when (state.sortType) {
                    SortType.NAME -> dao.getAll()
                    SortType.END_DATE -> dao.getAllOrderedByEndDate()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    val state = combine(_state, _healthBars) { state, healthBars ->
        state.copy(healthBars = healthBars)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.Sort -> {
                _state.update { it.copy(
                    sortType = event.sortType,
                ) }
            }
            ListEvent.BeginSearch -> {
                _state.update { it.copy(
                    isSearching = true,
                    query = "",
                ) }
            }
            is ListEvent.SetQuery -> {
                _state.update { it.copy(
                    query = event.query,
                ) }
            }
            ListEvent.EndSearch -> {
                _state.update { it.copy(
                    isSearching = false,
                    query = "",
                ) }
            }
        }
    }
}