package ru.practicum.android.diploma.filters.choosearea.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.common.domain.Resource
import ru.practicum.android.diploma.filters.choosearea.data.dto.AreasCatalogDto
import ru.practicum.android.diploma.filters.choosearea.data.dto.AreasCatalogRequest
import ru.practicum.android.diploma.filters.choosearea.data.dto.AreasCatalogResponse
import ru.practicum.android.diploma.filters.choosearea.data.dto.AreasRequest
import ru.practicum.android.diploma.filters.choosearea.data.storage.AreasStorageApi
import ru.practicum.android.diploma.filters.choosearea.domain.api.ChooseAreaRepository
import ru.practicum.android.diploma.filters.choosearea.domain.models.AreaInfo
import ru.practicum.android.diploma.filters.choosearea.domain.models.AreasResult
import ru.practicum.android.diploma.filters.choosearea.domain.models.CountriesResult
import ru.practicum.android.diploma.filters.choosearea.domain.models.CountryInfo

class ChooseAreaRepositoryImpl(
    private val networkClient: NetworkClient,
    private val areasStorageApi: AreasStorageApi,
) : ChooseAreaRepository {

    companion object {
        const val OTHER_REGIONS = "Другие регионы"
    }

    private val comparator = Comparator { o1: CountryInfo, o2: CountryInfo ->
        when {
            o1.name == OTHER_REGIONS -> 1
            o2.name == OTHER_REGIONS -> -1
            else -> 0
        }
    }

    override fun getCountries(): Flow<Resource<CountriesResult?>> = flow {
        when (val response = networkClient.doRequest(AreasRequest())) {
            is AreasCatalogResponse -> {
                emit(
                    Resource
                        .Success(
                            CountriesResult(
                                countries = response.areasCatalog.map(::convertCountry).sortedWith(comparator)
                            )
                        )
                )
            }

            else -> {
                emit(Resource.Error(response.errorType))
            }
        }
    }

    override fun getAreaByParentId(areaId: String): Flow<Resource<AreasResult?>> = flow {
        when (val response = networkClient.doRequest(AreasCatalogRequest(areaId))) {
            is AreasCatalogDto -> {
                emit(
                    Resource
                        .Success(
                            AreasResult(
                                areas = convertArea(response)
                            )
                        )
                )
            }

            else -> {
                emit(Resource.Error(response.errorType))
            }
        }
    }

    override fun getAreasWithCountries(): Flow<Resource<AreasResult?>> = flow {
        when (val response = networkClient.doRequest(AreasRequest())) {
            is AreasCatalogResponse -> {
                val areas: MutableList<AreaInfo> = mutableListOf()
                for (area in response.areasCatalog) {
                    areas.addAll(convertArea(area))
                }
                emit(
                    Resource
                        .Success(
                            AreasResult(
                                areas = areas
                            )
                        )
                )
            }

            else -> {
                emit(Resource.Error(response.errorType))
            }
        }
    }

    override fun saveAreaSettings(area: AreaInfo) {
        areasStorageApi.writeArea(area)
    }

    override fun getAreaSettings(): AreaInfo? {
        return areasStorageApi.readArea()
    }

    override fun deleteAreaSettings() {
        areasStorageApi.removeArea()
    }

    override fun savePreviousAreaSettings(area: AreaInfo) {
        areasStorageApi.writePreviousArea(area)
    }

    override fun getPreviousAreaSettings(): AreaInfo? {
        return areasStorageApi.readPreviousArea()
    }

    override fun deletePreviousAreaSettings() {
        areasStorageApi.removePreviousArea()
    }

    private fun convertCountry(it: AreasCatalogDto): CountryInfo =
        CountryInfo(
            it.id,
            it.name,
        )

    private fun convertArea(it: AreasCatalogDto): List<AreaInfo> {
        val country = CountryInfo(
            id = it.id,
            name = it.name
        )
        val areas: MutableList<AreaInfo> = mutableListOf()
        for (area in it.areas) {
            areas.add(AreaInfo(area.id, area.name, country))
            for (subArea in area.areas) {
                areas.add(AreaInfo(subArea.id, subArea.name, country))
            }
        }
        return areas
    }

}
