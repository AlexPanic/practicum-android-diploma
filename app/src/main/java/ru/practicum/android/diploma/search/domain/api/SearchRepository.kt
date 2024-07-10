package ru.practicum.android.diploma.search.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.common.domain.Resource
import ru.practicum.android.diploma.search.domain.models.SearchResult

interface SearchRepository {
    fun findVacancies(expression: String, page: Int?, perPage: Int?): Flow<Resource<SearchResult?>>
}
