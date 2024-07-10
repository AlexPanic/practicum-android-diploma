package ru.practicum.android.diploma.search.presentation.models

import ru.practicum.android.diploma.common.domain.ErrorType
import ru.practicum.android.diploma.common.domain.VacancyBase

sealed interface SearchState {

    data object Loading : SearchState
    data object Empty : SearchState
    data class Content(val vacancies: MutableList<VacancyBase>) : SearchState
    data class Error(val errorType: ErrorType) : SearchState
}
