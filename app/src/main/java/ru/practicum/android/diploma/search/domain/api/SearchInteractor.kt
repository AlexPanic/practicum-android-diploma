package ru.practicum.android.diploma.search.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.common.domain.ErrorType
import ru.practicum.android.diploma.search.domain.models.SearchResult

interface SearchInteractor {
    fun findVacancies(expression: String, page: Int?, perPage: Int?): Flow<Pair<SearchResult?, ErrorType>>
}
