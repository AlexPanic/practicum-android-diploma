package ru.practicum.android.diploma.search.domain.models

import ru.practicum.android.diploma.common.domain.VacancyBase

class VacancySearchResultItem(
    id: Int,
    hhID: String,
    name: String,
    isFavorite: Boolean,
    val employerInfo: EmployerInfo,
    val salaryInfo: SalaryInfo
) : VacancyBase(
    id,
    hhID,
    name,
    isFavorite
)

class EmployerInfo(
    val employerName: String,
    val employerLogoUrl: String,
    val areaName: String
)

class SalaryInfo(
    val salaryFrom: Int,
    val salaryTo: Int?,
    val salaryCurrency: String
)
