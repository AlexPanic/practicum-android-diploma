package ru.practicum.android.diploma.favorites.data.converters

import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.favorites.data.db.VacancyEntity
import ru.practicum.android.diploma.favorites.domain.models.FavouriteVacanciesModel

class VacancyDbConverter {
    fun map(vacancy: VacancyBase): VacancyEntity {
        return VacancyEntity(
            vacancy.hhID,
            vacancy.name,
            vacancy.isFavorite,
            vacancy.employerInfo.areaName,
            vacancy.employerInfo.employerName,
            vacancy.employerInfo.employerLogoUrl ?: "",
            vacancy.salaryInfo?.salaryTo ?: 0,
            vacancy.salaryInfo?.salaryFrom ?: 0,
            vacancy.salaryInfo?.salaryCurrency ?: ""
        )
    }

    fun map(entity: VacancyEntity): FavouriteVacanciesModel {
        return FavouriteVacanciesModel(
            entity.hhID,
            entity.name,
            entity.isFavorite,
            entity.areaName,
            entity.employerName,
            entity.employerLogoUrl,
            entity.salaryTo,
            entity.salaryFrom,
            entity.salaryCurrency
        )
    }
}
