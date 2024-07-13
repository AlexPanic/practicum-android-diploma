package ru.practicum.android.diploma.vacancydetails.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.common.data.Success
import ru.practicum.android.diploma.vacancydetails.domain.api.DetailsInteractor
import ru.practicum.android.diploma.vacancydetails.domain.models.DetailsNotFoundType
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails
import ru.practicum.android.diploma.vacancydetails.presentation.models.DetailsState

class DetailsViewModel(
    private val vacancyInteractor: DetailsInteractor,
) : ViewModel() {

    private var isFavorite = MutableLiveData<Boolean>()
    fun observeFavoriteState(): LiveData<Boolean> = isFavorite

    private val vacancyState = MutableLiveData<DetailsState>()
    fun observeVacancyState(): LiveData<DetailsState> = vacancyState

    fun getVacancy(vacancyId: String) {
        renderState(DetailsState.Loading)
        viewModelScope.launch {
            vacancyInteractor.getVacancyDetail(vacancyId).collect { pair ->
                processResult(pair.first, pair.second)
            }
        }
    }

    private fun processResult(vacancyDetails: VacancyDetails?, errorType: ErrorType) {
        when (errorType) {
            is Success -> {
                if (vacancyDetails != null) {
                    renderState(
                        DetailsState.Content(vacancyDetails)
                    )
                } else {
                    renderState(
                        DetailsState.Empty
                    )
                }
            }

            is DetailsNotFoundType -> {
                renderState(
                    DetailsState.Empty
                )
            }

            else -> {
                renderState(
                    DetailsState.Error(errorType)
                )
            }

        }
    }

    private fun renderState(state: DetailsState) {
        vacancyState.postValue(state)
    }

}
