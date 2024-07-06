package ru.practicum.android.diploma.search.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.search.domain.api.VacanciesInteractor
import ru.practicum.android.diploma.search.domain.api.VacanciesRepository
import ru.practicum.android.diploma.util.Resource
import ru.practicum.android.diploma.vacancydetail.presentation.models.Vacancy

class VacanciesInteractorImpl(private val repository: VacanciesRepository) : VacanciesInteractor {
    override fun findVacancies(expression: String, page: Int?, perPage: Int?): Flow<Pair<List<Vacancy>?, String?>> {
        return repository.findVacancies(expression, page, perPage).map { result ->
            when (result) {
                is Resource.Success -> {
                    Pair(result.data, null)
                }

                is Resource.Error -> {
                    Pair(null, result.message)
                }
            }
        }
    }
}
