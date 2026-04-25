package com.example.notesapp.database
import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory actual constructor(context: Any) {
    actual fun createDriver(): SqlDriver {
        throw Exception("Not implemented for iOS")
    }
}