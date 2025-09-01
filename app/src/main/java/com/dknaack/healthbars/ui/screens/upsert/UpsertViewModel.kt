package com.dknaack.healthbars.ui.screens.upsert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dknaack.healthbars.data.HealthBar
import com.dknaack.healthbars.data.HealthBarDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class UpsertViewModel(
    private val dao: HealthBarDao,
): ViewModel() {
    private val _dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val _state = MutableStateFlow(UpsertState(
        startDate = _dateFormatter.format(LocalDate.now())
    ))

    val state = _state.asStateFlow()

    fun parseDateFlexible(input: String): LocalDate? {
        val formats = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("MMMM d, yyyy"),
            DateTimeFormatter.ofPattern("MMM d, yyyy"),
        )

        for (formatter in formats) {
            try {
                return LocalDate.parse(input, formatter)
            } catch (_: Exception) {
            }
        }
        return null // parsing failed
    }

    fun onEvent(event: UpsertEvent) {
        _state.update { it.copy(isDirty = true) }

        when (event) {
            is UpsertEvent.Save -> {
                try {
                    val duration = _state.value.duration.toLong()
                    val startDate = parseDateFlexible(_state.value.startDate)!!
                    val endDate = startDate.plus(duration, _state.value.durationUnit)

                    val healthBar = HealthBar(
                        id = _state.value.id,
                        name = _state.value.name,
                        startDate = startDate,
                        endDate = endDate,
                    )

                    viewModelScope.launch {
                        dao.upsert(healthBar)
                    }

                    _state.update { UpsertState() }
                } catch (e: DateTimeParseException) {
                    throw e
                }
            }
            is UpsertEvent.Discard -> {
                _state.update { UpsertState() }
            }
            is UpsertEvent.SetId -> {
                viewModelScope.launch {
                    dao.get(event.id).collect { hb ->
                        if (hb != null) {
                            _state.update {
                                it.copy(
                                    id = event.id,
                                    name = hb.name,
                                    startDate = _dateFormatter.format(hb.startDate),
                                    duration = (hb.endDate.toEpochDay() - hb.startDate.toEpochDay()).toString(),
                                    durationUnit = ChronoUnit.DAYS,
                                    isDirty = false,
                                )
                            }
                        } else {
                            _state.update { UpsertState() }
                        }
                    }
                }
            }
            is UpsertEvent.SetName -> {
                _state.update { it.copy(name = event.name) }
            }
            is UpsertEvent.SetStartDate -> {
                println("Setting start date")
                _state.update { it.copy(startDate = event.startDate) }
            }
            is UpsertEvent.SetStartDateMillis -> {
                event.value?.let { value ->
                    val instant = Instant.ofEpochMilli(value)
                    val zoneId = ZoneId.systemDefault()
                    val startDate = instant.atZone(zoneId).toLocalDate()

                    println("Updating state! ${_dateFormatter.format(startDate)}")
                    _state.update { it.copy(startDate = _dateFormatter.format(startDate)) }
                }
            }
            is UpsertEvent.SetDuration -> {
                // TODO: Set error when duration is incorrect string
                _state.update { it.copy(duration = event.duration) }
            }
            is UpsertEvent.SetDurationUnit -> {
                _state.update { it.copy(durationUnit = event.unit) }
            }
        }
    }
}