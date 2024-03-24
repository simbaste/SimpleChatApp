package com.example.simplechatapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplechatapp.viewmodel.SimpleChatViewModel

@Composable
fun DeviceDetailsScreen(viewModel: SimpleChatViewModel, deviceName: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Device Name: $deviceName")
        Button(
            onClick = {
                // Open Google
                viewModel.openGoogleApp()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Open Google")
        }
        Button(
            onClick = {
                // Open Hello World App
                viewModel.openHelloWorldApp()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Open Hello World App")
        }
    }
}
