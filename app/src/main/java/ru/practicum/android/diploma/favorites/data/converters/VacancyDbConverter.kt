package ru.practicum.android.diploma.favorites.data.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.common.domain.EmployerInfo
import ru.practicum.android.diploma.common.domain.NameInfo
import ru.practicum.android.diploma.common.domain.SalaryInfo
import ru.practicum.android.diploma.favorites.data.db.VacancyEntity
import ru.practicum.android.diploma.vacancydetails.domain.models.Address
import ru.practicum.android.diploma.vacancydetails.domain.models.Contacts
import ru.practicum.android.diploma.vacancydetails.domain.models.Details
import ru.practicum.android.diploma.vacancydetails.domain.models.Phone
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class VacancyDbConverter {
    fun map(vacancy: VacancyDetails): VacancyEntity {
        return VacancyEntity(
            hhID = vacancy.hhID,
            name = vacancy.name,
            isFavorite = vacancy.isFavorite,
            areaName = vacancy.employerInfo.areaName,
            employerName = vacancy.employerInfo.employerName,
            employerLogoUrl = vacancy.employerInfo.employerLogoUrl ?: "",
            salaryTo = vacancy.salaryInfo?.salaryTo ?: 0,
            salaryFrom = vacancy.salaryInfo?.salaryFrom ?: 0,
            salaryCurrency = vacancy.salaryInfo?.salaryCurrency ?: "",
            addressCity = vacancy.details.address?.city,
            addressBuilding = vacancy.details.address?.building,
            addressStreet = vacancy.details.address?.street,
            addressDescription = vacancy.details.address?.description,
            experienceId = vacancy.details.experience?.id,
            experienceName = vacancy.details.experience?.name,
            employmentId = vacancy.details.employment?.id,
            employmentName = vacancy.details.employment?.name,
            scheduleId = vacancy.details.schedule?.id,
            scheduleName = vacancy.details.schedule?.name,
            description = vacancy.details.description,
            keySkills = mapKeySkills(vacancy.details.keySkill),
            contactEmail = vacancy.details.contacts?.email,
            contactName = vacancy.details.contacts?.name,
            contactPhoneCity = mapContactPhoneCity(vacancy.details.contacts?.phone),
            contactPhoneComment = mapContactPhoneComment(vacancy.details.contacts?.phone),
            contactPhoneCountry = mapContactPhoneCountry(vacancy.details.contacts?.phone),
            contactPhoneFormatted = mapContactPhoneFormatted(vacancy.details.contacts?.phone),
            contactPhoneNumber = mapContactPhoneNumber(vacancy.details.contacts?.phone),
            hhVacancyLink = vacancy.details.hhVacancyLink
        )
    }

    private fun mapKeySkills(keySkills: List<String>?): String {
        return Gson().toJson(keySkills)
    }

    private fun mapContactPhoneCity(phone: List<Phone>?) = phone?.firstOrNull()?.city

    private fun mapContactPhoneComment(phone: List<Phone>?) = phone?.firstOrNull()?.comment

    private fun mapContactPhoneCountry(phone: List<Phone>?) = phone?.firstOrNull()?.country

    private fun mapContactPhoneFormatted(phone: List<Phone>?) = phone?.firstOrNull()?.formatted

    private fun mapContactPhoneNumber(phone: List<Phone>?) = phone?.firstOrNull()?.number

    fun map(vacancy: VacancyEntity): VacancyDetails {
        return VacancyDetails(
            hhID = vacancy.hhID,
            name = vacancy.name,
            isFavorite = vacancy.isFavorite,
            employerInfo = mapEmployerInfo(vacancy),
            salaryInfo = mapSalaryInfo(vacancy),
            details = mapDetails(vacancy)
        )
    }

    private fun mapEmployerInfo(vacancy: VacancyEntity): EmployerInfo {
        return EmployerInfo(
            areaName = vacancy.areaName,
            employerName = vacancy.employerName,
            employerLogoUrl = vacancy.employerLogoUrl
        )
    }

    private fun mapSalaryInfo(vacancy: VacancyEntity): SalaryInfo {
        return SalaryInfo(
            salaryTo = vacancy.salaryTo,
            salaryFrom = vacancy.salaryFrom,
            salaryCurrency = vacancy.salaryCurrency
        )
    }

    private fun mapDetails(vacancy: VacancyEntity): Details {
        return Details(
            address = mapAddress(vacancy),
            experience = mapExperience(vacancy),
            employment = mapEmployment(vacancy),
            schedule = mapSchedule(vacancy),
            description = vacancy.description,
            keySkill = mapKeySkills(vacancy.keySkills),
            contacts = mapContacts(vacancy),
            hhVacancyLink = vacancy.hhVacancyLink
        )
    }

    private fun mapAddress(vacancy: VacancyEntity): Address {
        return Address(
            city = vacancy.addressCity,
            building = vacancy.addressBuilding,
            street = vacancy.addressStreet,
            description = vacancy.addressDescription
        )
    }

    private fun mapExperience(vacancy: VacancyEntity): NameInfo {
        return NameInfo(
            id = vacancy.experienceId ?: "",
            name = vacancy.experienceName ?: ""
        )
    }

    private fun mapEmployment(vacancy: VacancyEntity): NameInfo {
        return NameInfo(
            id = vacancy.employmentId ?: "",
            name = vacancy.employmentName ?: ""
        )
    }

    private fun mapSchedule(vacancy: VacancyEntity): NameInfo {
        return NameInfo(
            id = vacancy.scheduleId ?: "",
            name = vacancy.scheduleName ?: ""
        )
    }

    private fun mapKeySkills(keySkills: String): List<String> {
        return Gson().fromJson(keySkills, object : TypeToken<List<String>>() {}.type)
    }

    private fun mapContacts(vacancy: VacancyEntity): Contacts {
        return Contacts(
            email = vacancy.contactEmail,
            name = vacancy.contactName,
            phone = listOf(
                Phone(
                    city = vacancy.contactPhoneCity ?: "",
                    comment = vacancy.contactPhoneComment,
                    country = vacancy.contactPhoneCountry ?: "",
                    formatted = vacancy.contactPhoneFormatted ?: "",
                    number = vacancy.contactPhoneNumber ?: ""
                )
            )
        )
    }
}
