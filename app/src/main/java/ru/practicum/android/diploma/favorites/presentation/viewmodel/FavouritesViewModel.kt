package ru.practicum.android.diploma.favorites.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyInteractor
import ru.practicum.android.diploma.favorites.presentation.models.FavouritesStates
import java.io.IOException

class FavouritesViewModel(
    private val favouriteVacancyInteractor: FavouriteVacancyInteractor
) : ViewModel() {
    private val _state = MutableLiveData<FavouritesStates>()
    val state: LiveData<FavouritesStates> = _state

    fun getAllFavouriteVacanciesView() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    favouriteVacancyInteractor
                        .getAllFavouritesVacancies()
                        .collect { vacancies ->
                            if (vacancies.isEmpty()) {
                                _state.postValue(FavouritesStates.Empty)
                            } else {
                                val vacancyBases = vacancies.map { vacancy ->
                                    VacancyBase(
                                        hhID = vacancy.hhID,
                                        name = vacancy.name,
                                        isFavorite = vacancy.isFavorite,
                                        employerInfo = vacancy.employerInfo,
                                        salaryInfo = vacancy.salaryInfo
                                    )
                                }
                                val vacancyArrayList = ArrayList(vacancyBases)
                                _state.postValue(FavouritesStates.NotEmpty(vacancyArrayList))
                            }
                        }
                } catch (e: IOException) {
                    _state.postValue(FavouritesStates.Error(e))
                }
            }
        }
    }
}
