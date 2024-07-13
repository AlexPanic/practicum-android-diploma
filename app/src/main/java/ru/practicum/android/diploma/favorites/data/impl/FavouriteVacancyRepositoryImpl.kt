package ru.practicum.android.diploma.favorites.data.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.favorites.data.converters.VacancyDbConverter
import ru.practicum.android.diploma.favorites.data.db.VacancyDatabase
import ru.practicum.android.diploma.favorites.data.db.VacancyEntity
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyRepository
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class FavouriteVacancyRepositoryImpl(
    private val vacancyDatabase: VacancyDatabase,
    private val vacancyDbConverter: VacancyDbConverter
) : FavouriteVacancyRepository {
    override suspend fun addVacancyToFavourite(vacancyId: VacancyDetails) {
        val vacancyEntity = convertToVacancyEntity(vacancyId)
        vacancyDatabase.vacancyDao().addVacancyToFavourite(vacancyEntity)
    }

    override suspend fun deleteVacancyFromFavourite(vacancyId: VacancyDetails) {
        val vacancyEntity = convertToVacancyEntity(vacancyId)
        vacancyDatabase.vacancyDao().deleteVacancyFromFavourite(vacancyEntity)
    }

    override fun getAllFavouritesVacancies(): Flow<ArrayList<VacancyDetails>> {
        return vacancyDatabase.vacancyDao().getAllFavouritesVacancies()
            .map { vacancy -> convertToVacancyDetails(vacancy) }
    }

    override fun getAllFavouritesVacanciesId(): Flow<List<String>> = flow {
        val vacancyId = vacancyDatabase.vacancyDao().getAllFavouritesVacanciesId()
        emit(vacancyId)
    }


    private fun convertToVacancyEntity(track: VacancyDetails): VacancyEntity {
        return vacancyDbConverter.map(track)
    }

    private fun convertToVacancyDetails(vacancies: List<VacancyEntity>): ArrayList<VacancyDetails> {
        return ArrayList(vacancies.map { vacancy -> vacancyDbConverter.map(vacancy) })
    }
}
