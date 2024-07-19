package ru.practicum.android.diploma.filters.chooseindustry.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.common.data.Resource
import ru.practicum.android.diploma.filters.chooseindustry.domain.interfaces.IndustryInteractor
import ru.practicum.android.diploma.filters.chooseindustry.domain.interfaces.IndustryRepository
import ru.practicum.android.diploma.filters.chooseindustry.domain.model.IndustriesResult

class IndustryInteractorImpl(private val repository: IndustryRepository) : IndustryInteractor {
    override fun getIndustries(): Flow<Pair<IndustriesResult?, ErrorType>> {
        return repository.getIndustries().map { result ->
            when (result) {
                is Resource.Success -> {
                    Pair(result.data, result.error)
                }
                is Resource.Error -> {
                    Pair(null, result.error)
                }
            }
        }
    }
}
