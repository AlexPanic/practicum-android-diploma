package ru.practicum.android.diploma.favorites.presentation.models

import ru.practicum.android.diploma.common.domain.VacancyBase
import java.io.IOException

sealed interface FavouritesStates {
    object Empty : FavouritesStates
    class NotEmpty(val vacancies: ArrayList<VacancyBase>) : FavouritesStates
    data class Error(val errorType: IOException) : FavouritesStates

}
