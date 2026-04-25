package com.example.notesapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notesapp.database.DatabaseDriverFactory
import com.example.notesapp.database.NotesDatabase
import com.example.notesapp.database.Note
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val database = remember { NotesDatabase(driverFactory.createDriver()) }
    val notesQueries = database.noteQueries

    // State untuk Input
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<Long?>(null) } // Untuk melacak apakah sedang Edit atau Baru

    // READ & SEARCH: Mengambil data berdasarkan query pencarian
    val notesList by remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            notesQueries.selectAll()
        } else {
            notesQueries.search(searchQuery)
        }.asFlow().mapToList(Dispatchers.IO)
    }.collectAsState(initial = emptyList())

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Notes KMP (CRUD)") }) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

                // --- BAGIAN SEARCH ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari Catatan...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- BAGIAN INPUT (CREATE & UPDATE) ---
                Card(elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(if (selectedNoteId == null) "Tambah Baru" else "Edit Catatan", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Judul") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Isi") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (noteTitle.isNotBlank()) {
                                        if (selectedNoteId == null) {
                                            // CREATE
                                            notesQueries.insert(noteTitle, noteContent, 0L)
                                        } else {
                                            // UPDATE
                                            notesQueries.update(noteTitle, noteContent, selectedNoteId!!)
                                        }
                                        // Reset Form
                                        noteTitle = ""; noteContent = ""; selectedNoteId = null
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (selectedNoteId == null) "Simpan" else "Update")
                            }

                            if (selectedNoteId != null) {
                                OutlinedButton(onClick = {
                                    noteTitle = ""; noteContent = ""; selectedNoteId = null
                                }) {
                                    Text("Batal")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BAGIAN LIST (READ & DELETE) ---
                LazyColumn {
                    items(notesList) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // Klik untuk UPDATE: Pindahkan data ke TextField
                                    noteTitle = note.title
                                    noteContent = note.content
                                    selectedNoteId = note.id
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = note.title, style = MaterialTheme.typography.headlineSmall)
                                    Text(text = note.content)
                                }
                                // Tombol DELETE
                                IconButton(onClick = { notesQueries.delete(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}