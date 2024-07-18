package ru.practicum.android.diploma.common.domain

open class VacancyBase(
    val hhID: String,
    val name: String,
    var isFavorite: Boolean,
    val employerInfo: EmployerInfo,
    val salaryInfo: SalaryInfo?,
)
