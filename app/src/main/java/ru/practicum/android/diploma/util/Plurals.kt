package ru.practicum.android.diploma.util

import android.content.res.Resources
import ru.practicum.android.diploma.R

fun getCountableVacancies(count: Int, resources: Resources): String {
    return resources.getQuantityString(R.plurals.vacancies_counter, count, count)
}
