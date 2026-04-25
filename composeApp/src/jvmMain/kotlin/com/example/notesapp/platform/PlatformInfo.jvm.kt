package com.example.notesapp.platform

actual class DeviceInfo actual constructor() {
    actual fun getDeviceModel(): String {
        return "Desktop (JVM) - ${System.getProperty("os.name")} ${System.getProperty("os.version")}"
    }
}

actual class NetworkMonitor {
    actual val isConnected: Boolean
        get() = true // Untuk Desktop sementara kita set true
}