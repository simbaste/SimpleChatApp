package com.example.simplechatapp.repository

import android.content.Context
import com.example.simplechatapp.models.Device
import kotlinx.coroutines.flow.SharedFlow

interface SimpleChatRepository {

    val connectedDevices: SharedFlow<List<Device>>
    val nearbyDevices: SharedFlow<List<Device>>
    val selectedDevice: SharedFlow<Device>

    fun sendMessage(message: String)
    fun search(context: Context)
    fun selectDevice(device: Device)
    fun openHelloWorldApp()
    fun openGoogleApp()

}