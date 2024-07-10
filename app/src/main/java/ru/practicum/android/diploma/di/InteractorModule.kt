package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.favorites.domain.db.FavouriteVacancyInteractor
import ru.practicum.android.diploma.favorites.domain.impl.FavouriteVacancyInteractorImpl
import ru.practicum.android.diploma.search.domain.api.SearchInteractor
import ru.practicum.android.diploma.search.domain.impl.SearchInteractorImpl
import ru.practicum.android.diploma.vacancydetails.domain.api.DetailsInteractor
import ru.practicum.android.diploma.vacancydetails.domain.impl.DetailsInteractorImpl

val interactorModule = module {
    factory<SearchInteractor> {
        SearchInteractorImpl(get())
    }


    factory<DetailsInteractor> {
        DetailsInteractorImpl(get())

    single<FavouriteVacancyInteractor> {
        FavouriteVacancyInteractorImpl(get())

    }
}
