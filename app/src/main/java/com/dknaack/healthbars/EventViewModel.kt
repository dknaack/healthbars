package com.dknaack.healthbars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dknaack.healthbars.data.HealthBarDao
import kotlinx.coroutines.launch

class EventViewModel(
    private val dao: HealthBarDao,
): ViewModel() {
    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.CreateHealthBar -> {

            }
            is UiEvent.ViewHealthBar -> {

            }
            is UiEvent.DeleteHealthBar -> {
                viewModelScope.launch {
                    dao.get(event.id)?.let { dao.delete(it) }
                }
            }
            is UiEvent.UpsertHealthBar -> {
                viewModelScope.launch {
                    dao.upsert(healthBar = event.healthBar)
                }
            }
        }
    }
}