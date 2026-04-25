package com.example.notesapp.platform

expect class DeviceInfo() {
    fun getDeviceModel(): String
}

expect class NetworkMonitor {
    val isConnected: Boolean
}