package ru.practicum.android.diploma.vacancydetails.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.common.domain.ErrorType
import ru.practicum.android.diploma.common.domain.NoInternetError
import ru.practicum.android.diploma.common.domain.Success
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyInteractor
import ru.practicum.android.diploma.vacancydetails.domain.api.DetailsInteractor
import ru.practicum.android.diploma.vacancydetails.domain.models.DetailsNotFoundType
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails
import ru.practicum.android.diploma.vacancydetails.presentation.models.DetailsState

class DetailsViewModel(
    private val vacancyInteractor: DetailsInteractor,
    private val favouriteVacancyInteractor: FavouriteVacancyInteractor,
) : ViewModel() {

    private var isFavourite: Boolean = false
    private val _vacancyExists = MutableLiveData<Boolean>()
    val vacancyExists: LiveData<Boolean> get() = _vacancyExists

    private val vacancyState = MutableLiveData<DetailsState>()
    fun observeVacancyState(): LiveData<DetailsState> = vacancyState
    private var favouriteVacanciesId: List<String>? = null
    var vacancy: VacancyDetails? = null

    fun addToFavById(vacancyId: VacancyDetails) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favouriteVacancyInteractor.addVacancyToFavourite(vacancyId)
            }
            isFavourite = true
            vacancyState.postValue(DetailsState.isFavorite(isFavourite))
        }
    }

    fun checkVacancyInDatabase(vacancyId: String) {
        viewModelScope.launch {
            val exists = withContext(Dispatchers.IO) {
                favouriteVacancyInteractor.getVacancyById(vacancyId) != null
            }
            _vacancyExists.postValue(exists)
        }
    }

    fun getVacancyDatabase(vacancyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val vacancy = favouriteVacancyInteractor.getVacancyById(vacancyId)
                withContext(Dispatchers.Main) {
                    vacancy?.let {
                        vacancyState.postValue(DetailsState.Content(it))
                    } ?: run {
                        vacancyState.postValue(DetailsState.Empty)
                    }
                }
            }
        }
    }

    fun deleteFavouriteVacancy(vacancyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favouriteVacancyInteractor.deleteVacancyFromFavourite(vacancyId)
            }
            isFavourite = false
            vacancyState.postValue(DetailsState.isFavorite(isFavourite))
        }
    }

    fun isFavourite(vacancyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favouriteVacancyInteractor
                    .getAllFavouritesVacanciesId()
                    .collect {
                        favouriteVacanciesId = it
                    }
            }
            if (favouriteVacanciesId != null) {
                if (favouriteVacanciesId!!.contains(vacancyId)) {
                    isFavourite = true
                    vacancyState.postValue(DetailsState.isFavorite(isFavourite))
                } else {
                    isFavourite = false
                    vacancyState.postValue(DetailsState.isFavorite(isFavourite))
                }
            }
        }
    }

    fun getVacancy(vacancyId: String) {
        if (vacancyId.isNotEmpty()) {
            renderState(DetailsState.Loading)
            viewModelScope.launch {
                vacancyInteractor.getVacancyDetail(vacancyId).collect { pair ->
                    processResult(pair.first, pair.second)
                }
            }
        }
    }

    private fun processResult(vacancyDetails: VacancyDetails?, errorType: ErrorType) {
        when (errorType) {
            is Success -> {
                if (vacancyDetails != null) {
                    vacancy = vacancyDetails
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

            is NoInternetError -> {
                renderState(
                    DetailsState.NoInternet
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

    fun getFavouriteState(): Boolean {
        return isFavourite
    }

    fun getSharingIntent(): Intent? {
        return if (vacancy != null) {
            vacancyInteractor.shareVacancy(vacancy!!.details.hhVacancyLink)
        } else {
            null
        }
    }

}
