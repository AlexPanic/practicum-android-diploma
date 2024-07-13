package ru.practicum.android.diploma.favorites.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyInteractor
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyRepository
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class FavouriteVacancyInteractorImpl(private val favouriteVacancyRepository: FavouriteVacancyRepository) :
    FavouriteVacancyInteractor {
    override suspend fun addVacancyToFavourite(vacancyId: VacancyDetails) {
        favouriteVacancyRepository.addVacancyToFavourite(vacancyId)
    }

    override suspend fun deleteVacancyFromFavourite(vacancyId: VacancyDetails) {
        favouriteVacancyRepository.deleteVacancyFromFavourite(vacancyId)
    }

    override fun getAllFavouritesVacancies(): Flow<ArrayList<VacancyDetails>> {
        return favouriteVacancyRepository.getAllFavouritesVacancies()
    }

    override fun getAllFavouritesVacanciesId(): Flow<List<String>> {
        return favouriteVacancyRepository.getAllFavouritesVacanciesId()
    }

}
