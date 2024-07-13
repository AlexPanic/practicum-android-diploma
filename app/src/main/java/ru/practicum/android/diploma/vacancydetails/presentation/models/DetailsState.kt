package ru.practicum.android.diploma.vacancydetails.presentation.models

import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

sealed interface DetailsState {
    data object Loading : DetailsState
    data object Empty : DetailsState
    data class Error(val errorType: ErrorType) : DetailsState
    data class Content(val vacancy: VacancyDetails) : DetailsState
}
