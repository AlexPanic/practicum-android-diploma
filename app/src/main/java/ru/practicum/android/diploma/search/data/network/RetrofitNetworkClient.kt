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
import java.io.IOException
import java.net.HttpURLConnection

class RetrofitNetworkClient(
    private val apiService: HhApiService,
    private val context: Context,
) : NetworkClient {

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
                            HttpURLConnection.HTTP_OK -> convertVacancySearchResponse(response.body())
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
            // выполняется, если в эмуляторе интернет смартфона включен, а на компьютере интернет отпал
            catch (e: IOException) {
                e.message?.let { Log.e("IO", it) }
                ResponseBase(ServerInternalError())
            }
        }
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

    private fun convertVacancySearchResponse(responseBody: VacancySearchResponse?): ResponseBase =
        if (responseBody == null || responseBody.items.isEmpty()) {
            ResponseBase(VacanciesNotFoundType())
        } else {
            VacancySearchResponse(
                responseBody.found,
                responseBody.page,
                responseBody.pages,
                responseBody.perPage,
                responseBody.items,
            )
        }

}
