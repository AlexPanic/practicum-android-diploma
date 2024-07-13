package ru.practicum.android.diploma.vacancydetails.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.common.data.Success
import ru.practicum.android.diploma.vacancydetails.domain.api.DetailsInteractor
import ru.practicum.android.diploma.vacancydetails.presentation.models.DetailsState

class DetailsViewModel(
    private val vacancyInteractor: DetailsInteractor,
    private val vacancyId: String
) : ViewModel() {

    private var isFavorite = MutableLiveData<Boolean>()
    fun observeFavoriteState(): LiveData<Boolean> = isFavorite

    private val vacancyState = MutableLiveData<DetailsState>()
    fun observeVacancyState(): LiveData<DetailsState> = vacancyState

    fun getVacancy() {
        vacancyState.postValue(DetailsState.Loading)
        viewModelScope.launch {
            vacancyInteractor.getVacancyDetail(vacancyId).collect { result ->
                when (result.second) {
                    is Success -> {
                        result.first?.let {
                            renderState(DetailsState.Content(it))
                        }
                    }

                    is Error -> {
                        renderState(DetailsState.Error(result.second))
                    }
                }
            }
        }
    }

    private fun renderState(state: DetailsState) {
        vacancyState.postValue(state)
    }

}
