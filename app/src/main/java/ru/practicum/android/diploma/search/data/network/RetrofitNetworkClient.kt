package ru.practicum.android.diploma.search.data.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.practicum.android.diploma.common.data.BadRequestError
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.common.data.NoInternetError
import ru.practicum.android.diploma.common.data.ResponseBase
import ru.practicum.android.diploma.common.data.ServerInternalError
import ru.practicum.android.diploma.search.data.HhQueryOptions
import ru.practicum.android.diploma.search.data.dto.VacancySearchRequest
import ru.practicum.android.diploma.search.data.dto.VacancySearchResponse
import ru.practicum.android.diploma.search.domain.models.VacanciesNotFoundType
import ru.practicum.android.diploma.util.isConnected
import java.net.HttpURLConnection

class RetrofitNetworkClient(
    private val apiService: HhApiService,
    private val context: Context,
) : NetworkClient {

    private fun searchOptions(dto: VacancySearchRequest): HashMap<String, String> {
        val options: HashMap<String, String> = HashMap()
        options[HhQueryOptions.TEXT.key] = dto.expression
        options[HhQueryOptions.SEARCH_FIELD.key] = "name" // поиск только по названию вакансии
        if (dto.page != null) {
            options[HhQueryOptions.PAGE.key] = dto.page.toString()
        }
        if (dto.perPage != null) {
            options[HhQueryOptions.PER_PAGE.key] = dto.perPage.toString()
        }
        return options
    }

    override suspend fun doRequest(dto: Any): ResponseBase {
        if (!isConnected(context)) {
            return ResponseBase(NoInternetError())
        }
        return withContext(Dispatchers.IO) {
            try {
                when (dto) {
                    is VacancySearchRequest -> {
                        val options = searchOptions(dto)
                        val response = apiService.findVacancies(options)
                        when (response.code()) {
                            HttpURLConnection.HTTP_OK -> {
                                val vacancySearchResponse = response.body()
                                if (vacancySearchResponse == null || vacancySearchResponse.items.isEmpty()) {
                                    ResponseBase(VacanciesNotFoundType())
                                } else {
                                    VacancySearchResponse(
                                        vacancySearchResponse.found,
                                        vacancySearchResponse.page,
                                        vacancySearchResponse.pages,
                                        vacancySearchResponse.perPage,
                                        vacancySearchResponse.items,
                                    )
                                }
                            }

                            HttpURLConnection.HTTP_NOT_FOUND -> ResponseBase(VacanciesNotFoundType())
                            else -> ResponseBase(BadRequestError())
                        }
                    }

                    else -> ResponseBase(BadRequestError())
                }

            } catch (e: HttpException) {
                e.message?.let { Log.e("Http", it) }
                ResponseBase(ServerInternalError())
            }
        }
    }
}
