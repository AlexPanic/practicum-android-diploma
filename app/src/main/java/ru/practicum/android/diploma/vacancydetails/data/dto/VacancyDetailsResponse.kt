package ru.practicum.android.diploma.vacancydetails.data.dto

import com.google.gson.annotations.SerializedName
import ru.practicum.android.diploma.common.data.ResponseBase

data class VacancyDetailsResponse(
    val id: String,
    val name: String,
    val area: AreaDto,
    val address: AddressDto?,
    val employer: EmployerDto?,
    val salary: SalaryDto?,
    val experience: NameInfoDto?,
    val employment: NameInfoDto?,
    val schedule: NameInfoDto?,
    val description: String,
    val keySkill: List<String>?,
    val contacts: ContactsDto?,
    @SerializedName("alternate_url")
    val hhVacancyLink: String,
    // @SerializedName("show_logo_in_search")
    // val employerLogoUrl: Boolean?
) : ResponseBase()

data class AreaDto(
    // val id: String,
    val name: String,
    // val url: String
)

data class AddressDto(
    val city: String?,
    val building: String?,
    val street: String?,
    val description: String?
)

data class ContactsDto(
    val email: String?,
    val name: String?,
    val phone: List<PhoneDto>?
)

data class PhoneDto(
    val city: String,
    val comment: String?,
    val country: String,
    val formatted: String,
    val number: String
)

data class EmployerDto(
    val id: String?,
    val name: String,
    @SerializedName("logo_urls")
    val logoUrls: LogoUrls?
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
    val to: Int?
)

data class NameInfoDto(
    val id: String?,
    val name: String
)

