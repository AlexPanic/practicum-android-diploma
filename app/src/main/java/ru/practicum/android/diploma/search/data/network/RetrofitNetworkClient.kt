package ru.practicum.android.diploma.search.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.common.data.ResponseBase
import ru.practicum.android.diploma.common.domain.BadRequestError
import ru.practicum.android.diploma.common.domain.NoInternetError
import ru.practicum.android.diploma.common.domain.ServerInternalError
import ru.practicum.android.diploma.search.data.HhQueryOptions
import ru.practicum.android.diploma.search.data.dto.VacancySearchRequest
import ru.practicum.android.diploma.search.data.dto.VacancySearchResponse
import ru.practicum.android.diploma.search.domain.models.VacancyNotFoundType
import java.net.HttpURLConnection

class RetrofitNetworkClient(
    private val apiService: HhApiService,
    private val context: Context,
) : NetworkClient {

    private fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
    }

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
        if (!isConnected()) {
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
                                    ResponseBase(VacancyNotFoundType())
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

                            HttpURLConnection.HTTP_NOT_FOUND -> ResponseBase(VacancyNotFoundType())
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
