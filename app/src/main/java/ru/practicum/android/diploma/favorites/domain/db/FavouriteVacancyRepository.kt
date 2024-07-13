package ru.practicum.android.diploma.favorites.domain.db

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

interface FavouriteVacancyRepository {
    suspend fun addVacancyToFavourite(vacancyId: VacancyDetails)
    suspend fun deleteVacancyFromFavourite(vacancyId: VacancyDetails)
    fun getAllFavouritesVacancies(): Flow<ArrayList<VacancyDetails>>
    fun getAllFavouritesVacanciesId(): Flow<List<String>>
}
