package com.example.notesapp.di

import com.example.notesapp.database.DatabaseDriverFactory
import com.example.notesapp.platform.DeviceInfo
import com.example.notesapp.database.NotesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    single {
        val driverFactory = get<DatabaseDriverFactory>()
        NotesDatabase(driverFactory.createDriver())
    }

    single { DeviceInfo() }
}