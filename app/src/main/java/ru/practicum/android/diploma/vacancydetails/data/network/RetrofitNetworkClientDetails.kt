package ru.practicum.android.diploma.vacancydetails.data.network

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
import ru.practicum.android.diploma.util.isConnected
import ru.practicum.android.diploma.vacancydetails.data.dto.VacancyDetailsRequest
import ru.practicum.android.diploma.vacancydetails.data.dto.VacancyDetailsResponse
import ru.practicum.android.diploma.vacancydetails.domain.models.DetailsNotFoundType
import java.io.IOException
import java.net.HttpURLConnection

class RetrofitNetworkClientDetails(
    private val hhApiServiceDetails: HhApiServiceDetails,
    private val context: Context,
) : NetworkClient {
    override suspend fun doRequest(dto: Any): ResponseBase {
        if (!isConnected(context)) {
            return ResponseBase(NoInternetError())
        }
        return withContext(Dispatchers.IO) {
            try {
                when (dto) {
                    is VacancyDetailsRequest -> {
                        val response = hhApiServiceDetails.getVacancyDetails(dto.vacancyID)
                        when (response.code()) {
                            HttpURLConnection.HTTP_OK -> convertVacancyDetailsResponse(response.body())
                            HttpURLConnection.HTTP_NOT_FOUND -> ResponseBase(DetailsNotFoundType())
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

    private fun convertVacancyDetailsResponse(responseBody: VacancyDetailsResponse?): ResponseBase =
        if (responseBody == null) {
            ResponseBase(DetailsNotFoundType())
        } else {
            VacancyDetailsResponse(
                responseBody.id,
                responseBody.name,
                responseBody.area,
                responseBody.address,
                responseBody.employer,
                responseBody.salary,
                responseBody.experience,
                responseBody.employment,
                responseBody.schedule,
                responseBody.description,
                responseBody.keySkill,
                responseBody.contacts,
                responseBody.hhVacancyLink
            )
        }
}
