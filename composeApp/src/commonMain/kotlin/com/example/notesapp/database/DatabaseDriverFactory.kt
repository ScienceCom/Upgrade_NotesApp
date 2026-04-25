package com.example.notesapp.database
import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory(context: Any) { // Pastikan 'Any'
    fun createDriver(): SqlDriver
}