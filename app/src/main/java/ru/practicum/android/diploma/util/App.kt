package ru.practicum.android.diploma.util

import android.app.Application
import com.markodevcic.peko.PermissionRequester
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.practicum.android.diploma.di.dataModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(dataModule)
        }

        PermissionRequester.initialize(applicationContext)
    }


}
