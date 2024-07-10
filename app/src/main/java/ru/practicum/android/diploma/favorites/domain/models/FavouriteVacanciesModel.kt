package ru.practicum.android.diploma.favorites.domain.models
data class FavouriteVacanciesModel(
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
