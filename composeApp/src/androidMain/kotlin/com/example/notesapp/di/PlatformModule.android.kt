package com.example.notesapp.di

import com.example.notesapp.database.DatabaseDriverFactory
import com.example.notesapp.platform.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.Module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }

    single { NetworkMonitor(androidContext()) }
}