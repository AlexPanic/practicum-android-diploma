package ru.practicum.android.diploma.filters.chooseindustry.domain.interfaces

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.common.domain.Resource
import ru.practicum.android.diploma.filters.chooseindustry.domain.model.IndustriesModel

interface IndustryRepository {
    fun getIndustries(): Flow<Resource<List<IndustriesModel>>>
    fun saveIndustrySettings(industry: IndustriesModel)
    fun getIndustrySettings(): IndustriesModel?
    fun deleteIndustrySettings()
    fun searchIndustries(query: String): Flow<Resource<List<IndustriesModel>>>
}
