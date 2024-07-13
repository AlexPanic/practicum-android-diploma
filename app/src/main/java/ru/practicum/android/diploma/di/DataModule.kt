package ru.practicum.android.diploma.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.favorites.data.converters.VacancyDbConverter
import ru.practicum.android.diploma.favorites.data.db.VacancyDatabase
import ru.practicum.android.diploma.search.data.network.HhApiService
import ru.practicum.android.diploma.search.data.network.RetrofitNetworkClient
import ru.practicum.android.diploma.vacancydetails.data.network.HhApiServiceDetails
import ru.practicum.android.diploma.vacancydetails.data.network.RetrofitNetworkClientDetails

val dataModule = module {
    single<HhApiService> {
        Retrofit.Builder()
            .baseUrl("https://api.hh.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HhApiService::class.java)
    }

    single<HhApiServiceDetails> {
        Retrofit.Builder()
            .baseUrl("https://api.hh.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HhApiServiceDetails::class.java)
    }

    single {
        Room.databaseBuilder(androidContext(), VacancyDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        androidContext().getSharedPreferences(
            androidContext().getString(R.string.shared_pref_storage_key),
            Context.MODE_PRIVATE
        )
    }
    factory { VacancyDbConverter() }

    factory { Gson() }

    single<NetworkClient>(named("search")) {
        RetrofitNetworkClient(get(), androidContext())
    }
    single<NetworkClient>(named("details")) {
        RetrofitNetworkClientDetails(get(), androidContext())
    }

}
