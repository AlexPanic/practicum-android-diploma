package ru.practicum.android.diploma.favorites.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 2, entities = [VacancyEntity::class], exportSchema = false)
abstract class VacancyDatabase : RoomDatabase() {
    abstract fun vacancyDao(): VacancyDao
}
