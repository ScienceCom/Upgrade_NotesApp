package com.example.notesapp.platform

import platform.UIKit.UIDevice

actual class DeviceInfo actual constructor() {
    actual fun getDeviceModel(): String {
        return "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}"
    }
}

actual class NetworkMonitor {
    actual val isConnected: Boolean = true
}