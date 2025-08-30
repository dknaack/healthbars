package com.dknaack.healthbars.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dknaack.healthbars.data.HealthBarDao
import kotlinx.coroutines.launch

class ListViewModel(
    private val dao: HealthBarDao,
): ViewModel() {
    fun onEvent(event: ListEvent) {
        when (event) {
            ListEvent.CreateHealthBar -> {

            }
            is ListEvent.ViewHealthBar -> {

            }
            is ListEvent.DeleteHealthBar -> {
                viewModelScope.launch {
                    dao.get(event.id)?.let { dao.delete(it) }
                }
            }
            is ListEvent.UpsertHealthBar -> {
                viewModelScope.launch {
                    dao.upsert(healthBar = event.healthBar)
                }
            }
        }
    }
}