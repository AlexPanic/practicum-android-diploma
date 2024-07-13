package ru.practicum.android.diploma.vacancydetails.domain.models

import ru.practicum.android.diploma.common.domain.EmployerInfo
import ru.practicum.android.diploma.common.domain.SalaryInfo

data class VacancyDetailsTest(
    val hhID: String,
    val name: String,
    val isFavorite: Boolean,
    val employerInfo: EmployerInfo,
    val salaryInfo: SalaryInfo?,
    val experience: String,
    val formatWork: String,
    val responsibilities: List<String>,
    val requirements: List<String>,
    val conditions: List<String>,
    val keySkills: List<String>?
)
