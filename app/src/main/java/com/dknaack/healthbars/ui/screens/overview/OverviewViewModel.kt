package com.dknaack.healthbars.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dknaack.healthbars.data.HealthBarDao
import com.dknaack.healthbars.data.LogAction
import com.dknaack.healthbars.data.LogDao
import com.dknaack.healthbars.data.LogEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

class OverviewViewModel(
    private val healthBarDao: HealthBarDao,
    private val logDao: LogDao,
): ViewModel() {
    private val _healthBarId = MutableStateFlow(0L)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _logEntries = _healthBarId.flatMapLatest { logDao.getLog(it) }
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _healthBar = _healthBarId.flatMapLatest { healthBarDao.get(it) }
    private val _state = MutableStateFlow(OverviewState())
    val state = combine(_state, _logEntries, _healthBar) { state, logEntries, healthBar ->
        state.copy(
            logEntries = logEntries,
            healthBar = healthBar,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverviewState())

    fun onEvent(event: OverviewEvent) {
        when (event) {
            is OverviewEvent.Show -> {
                _healthBarId.value = event.healthBarId
            }
            OverviewEvent.Skip -> {
                viewModelScope.launch {
                    state.value.healthBar?.let { healthBar ->
                        val period = Period.between(healthBar.startDate, healthBar.endDate)

                        healthBarDao.upsert(
                            healthBar.copy(
                                startDate = healthBar.endDate,
                                endDate = healthBar.endDate.plus(period)
                            )
                        )
                        logDao.insert(LogEntry(
                            healthBarId = _healthBarId.value,
                            action = LogAction.SKIP,
                        ))
                    }
                }
            }
            OverviewEvent.Refresh -> {
                viewModelScope.launch {
                    state.value.healthBar?.let { healthBar ->
                        val period = Period.between(healthBar.startDate, healthBar.endDate)
                        val startDate = LocalDate.now()

                        healthBarDao.upsert(
                            healthBar.copy(
                                startDate = startDate,
                                endDate = startDate.plus(period)
                            )
                        )
                        logDao.insert(LogEntry(
                            healthBarId = _healthBarId.value,
                            action = LogAction.REFRESH,
                        ))
                    }
                }
            }
        }
    }
}