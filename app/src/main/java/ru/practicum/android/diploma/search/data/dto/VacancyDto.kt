package ru.practicum.android.diploma.search.data.dto

import com.google.gson.annotations.SerializedName

data class VacancyDto(
    val id: String,
    val name: String,
    val area: AreaDto,
    val address: AddressDTO?,
    val employer: EmployerDto,
    val salary: SalaryDto?,
    // val employment: ReferenceDto?,
    // val experience: ReferenceDto?,
    // если на hh компания не хочет показать лого в поиске
    // значит и мы в приложении можем это учесть
    @SerializedName("show_logo_in_search")
    val showLogoInSearch: Boolean?,
)

data class AreaDto(
    val id: String,
    val name: String,
    val url: String,
)

data class AddressDTO(
    val city: String?,
)

data class EmployerDto(
    val id: String?,
    val name: String,
    @SerializedName("logo_urls")
    val logoUrls: LogoUrls?,
    val url: String?,
)

data class LogoUrls(
    @SerializedName("90")
    val logo90: String?,
    @SerializedName("240")
    val logo240: String?,
    val original: String?,
)

data class SalaryDto(
    val currency: String?,
    val from: Int?,
    val to: Int?,
    // Признак что границы зарплаты указаны до вычета налогов
    val gross: Boolean?,
)

// значение из справочника Hh
data class ReferenceDto(
    val id: String?,
    val name: String?,
)
