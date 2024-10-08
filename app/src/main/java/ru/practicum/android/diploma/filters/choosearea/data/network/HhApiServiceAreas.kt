package ru.practicum.android.diploma.filters.choosearea.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.practicum.android.diploma.filters.choosearea.data.dto.AreasCatalogDto

interface HhApiServiceAreas {
    @GET("/areas")
    suspend fun getAreas(): Response<List<AreasCatalogDto>>

    @GET("/areas/{area_id}")
    suspend fun getAreasByParentId(@Path("area_id") areaId: String?): Response<AreasCatalogDto>
}
