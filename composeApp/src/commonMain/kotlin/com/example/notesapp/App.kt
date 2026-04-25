package com.example.notesapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.notesapp.database.NotesDatabase
import com.example.notesapp.platform.DeviceInfo
import com.example.notesapp.platform.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val database: NotesDatabase = koinInject()
    val deviceInfo: DeviceInfo = koinInject()
    val networkMonitor: NetworkMonitor = koinInject()

    val notesQueries = database.noteQueries

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<Long?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val isOnline by remember { mutableStateOf(networkMonitor.isConnected) }

    val notesList by remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            notesQueries.selectAll()
        } else {
            notesQueries.search(searchQuery)
        }.asFlow().mapToList(Dispatchers.IO)
    }.collectAsState(initial = emptyList())

    MaterialTheme {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Notes KMP (CRUD)") },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(androidx.compose.material.icons.Icons.Default.Settings, "Settings")
                            }
                        }
                    )
                    // Network Indicator Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFE57373)
                    ) {
                        Text(
                            text = if (isOnline) "Koneksi: Online" else "Koneksi: Offline (Mode Lokal)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // --- BAGIAN SEARCH ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari Catatan...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- BAGIAN INPUT (CREATE & UPDATE) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedNoteId == null) "Tambah Baru" else "Edit Catatan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Judul") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Isi Catatan") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (noteTitle.isNotBlank()) {
                                        if (selectedNoteId == null) {
                                            notesQueries.insert(noteTitle, noteContent, 0L)
                                        } else {
                                            notesQueries.update(noteTitle, noteContent, selectedNoteId!!)
                                        }
                                        noteTitle = ""; noteContent = ""; selectedNoteId = null
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (selectedNoteId == null) "Simpan" else "Perbarui")
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
                Text("Daftar Catatan", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                // --- BAGIAN LIST (READ & DELETE) ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notesList) { note ->
                        NoteItem(
                            note = note,
                            onEdit = {
                                noteTitle = note.title
                                noteContent = note.content
                                selectedNoteId = note.id
                            },
                            onDelete = { notesQueries.delete(note.id) }
                        )
                    }
                }
            }
        }
    }
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Pengaturan & Info") },
            text = {
                Column {
                    Text("Informasi Perangkat:", style = MaterialTheme.typography.labelLarge)
                    Text(deviceInfo.getDeviceModel())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status Jaringan:", style = MaterialTheme.typography.labelLarge)
                    Text(if (isOnline) "Terhubung" else "Terputus")
                }
            },
            confirmButton = {
                Button(onClick = { showSettings = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun NoteItem(note: com.example.notesapp.database.Note, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}