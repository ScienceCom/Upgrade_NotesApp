package com.example.notesapp.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory actual constructor(context: Any) {

    private val androidContext = context as Context

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = NotesDatabase.Schema,
            context = androidContext,
            name = "notes.db"
        )
    }
}