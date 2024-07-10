package ru.practicum.android.diploma.favorites.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyInteractor
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyRepository
import ru.practicum.android.diploma.favorites.domain.models.FavouriteVacanciesModel

class FavouriteVacancyInteractorImpl(private val favouriteVacancyRepository: FavouriteVacancyRepository) :
    FavouriteVacancyInteractor {
    override suspend fun addVacancyToFavourite(vacancyId: VacancyBase) {
        favouriteVacancyRepository.addVacancyToFavourite(vacancyId)
    }

    override suspend fun deleteVacancyFromFavourite(vacancyId: VacancyBase) {
        favouriteVacancyRepository.deleteVacancyFromFavourite(vacancyId)
    }

    override fun getAllFavouritesVacancies(): Flow<List<FavouriteVacanciesModel>> {
        return favouriteVacancyRepository.getAllFavouritesVacancies()
    }

    override fun getAllFavouritesVacanciesId(): Flow<List<String>> {
        return favouriteVacancyRepository.getAllFavouritesVacanciesId()
    }
}
