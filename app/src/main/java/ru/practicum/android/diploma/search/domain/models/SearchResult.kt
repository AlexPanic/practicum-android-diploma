package ru.practicum.android.diploma.search.domain.models

import ru.practicum.android.diploma.common.domain.VacancyBase

class SearchResult(
    val page: Int,
    val perPage: Int,
    val pages: Int,
    val found: Int,
    val vacancies: List<VacancyBase>,
)
