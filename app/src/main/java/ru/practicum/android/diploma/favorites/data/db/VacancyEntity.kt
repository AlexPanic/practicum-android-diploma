package ru.practicum.android.diploma.favorites.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vacancy_table")
data class VacancyEntity(
    @PrimaryKey
    val hhID: String,
    val name: String,
    val isFavorite: Boolean,
    val areaName: String,
    val employerName: String,
    val employerLogoUrl: String,
    val salaryTo: Int,
    val salaryFrom: Int,
    val salaryCurrency: String
)
