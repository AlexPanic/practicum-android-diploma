package ru.practicum.android.diploma.filters.settingsfilters.data.repo

import ru.practicum.android.diploma.common.domain.FiltersAll
import ru.practicum.android.diploma.filters.settingsfilters.data.storage.SettingsStorageApi
import ru.practicum.android.diploma.filters.settingsfilters.domain.api.SettingsRepository
import ru.practicum.android.diploma.filters.settingsfilters.domain.models.SalaryFilters

class SettingsRepositoryImpl(private val settingsStorageApi: SettingsStorageApi) : SettingsRepository {
    override fun getSalaryFilters(): SalaryFilters? {
        return settingsStorageApi.readSalaryFilters()
    }

    override fun saveSalaryFilters(salaryFilters: SalaryFilters) {
        settingsStorageApi.writeSalaryFilters(salaryFilters)
    }

    override fun deleteSalaryFilters() {
        settingsStorageApi.removeSalaryFilters()
    }

    override fun getPreviousFilters(): FiltersAll? = settingsStorageApi.readPreviousFilters()

    override fun savePreviousFilters(filters: FiltersAll) {
        settingsStorageApi.writePreviousFilters(filters)
    }

    override fun deletePreviousFilters() {
        settingsStorageApi.removePreviousFilters()
    }
}
