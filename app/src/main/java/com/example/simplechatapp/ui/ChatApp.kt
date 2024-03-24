package com.example.simplechatapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.simplechatapp.models.Device
import com.example.simplechatapp.models.DevicesPlaceholder
import com.example.simplechatapp.viewmodel.SimpleChatViewModel

@Composable
fun ChatApp(viewModel: SimpleChatViewModel, navController: NavController) {

    var nearbyDevices by remember { mutableStateOf(emptyList<Device>()) }
    var connectedDevices by remember { mutableStateOf(emptyList<Device>()) }

    LaunchedEffect(key1 = viewModel) {
        viewModel.connectedDevices.collect {
            connectedDevices = it
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.nearbyDevices.collect {
            nearbyDevices = it
        }
    }

    ChatAppContent(
        nearbyDevices = nearbyDevices,
        connectedDevices = connectedDevices,
        onSend = {
            viewModel.sendMessage(it)
        },
        onDeviceSelected = {
            viewModel.selectDevice(it)
            navController.navigate("device/${it.name}")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppContent(
    nearbyDevices: List<Device>,
    connectedDevices: List<Device>,
    onSend: (String) -> Unit,
    onDeviceSelected: (Device) -> Unit
) {

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Smart TV Chat App") }) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        text = { Text(text = "Nearby Devices") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    Tab(
                        text = { Text(text = "Chat") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
                TabContent(
                    selectedTab = selectedTab,
                    nearbyDevices = nearbyDevices,
                    connectedDevices = connectedDevices,
                    onSend = onSend,
                    onDeviceSelected = onDeviceSelected
                )
            }
        }
    )
}

@Composable
fun TabContent(
    selectedTab: Int,
    nearbyDevices: List<Device>,
    connectedDevices: List<Device>,
    onSend: (String) -> Unit,
    onDeviceSelected: (Device) -> Unit
) {
    when(selectedTab) {
        0 -> NearbyDeviceTab(nearbyDevices, onDeviceSelected)
        1 -> ChatTab(connectedDevices, onSend)
    }
}


@Composable
fun NearbyDeviceTab(
    devices: List<Device>,
    onDeviceSelected: (Device) -> Unit
) {
    val navController = rememberNavController()
    // Implement the logic to display nearby devices
    Box(modifier = Modifier.padding(vertical = 20.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            content = {
                items(devices.size) { index ->
                    val device = devices[index]
                    NearbyDeviceItem(device = device, onClick = {
                        // Navigate to the second screen when the item is clicked
                        onDeviceSelected(device)
                    })
                    Divider(color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun NearbyDeviceItem(device: Device, onClick: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Share, contentDescription = "")
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = device.name)
    }
}

@Composable
fun ChatTab(
    connectedDevices: List<Device>,
    onSend: (String) -> Unit
) {

    var messageToSend by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ConnectedDevicesList(
            modifier = Modifier.padding(horizontal = 16.dp),
            devices = connectedDevices
        )

        OutlinedTextField(
            value = messageToSend,
            onValueChange = { messageToSend = it },
            label = { Text("Message") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend(messageToSend)
                    messageToSend = ""
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Button(
            onClick = {
                onSend(messageToSend)
                messageToSend = ""
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Text(text = "Send")
        }
    }
}

@Composable
fun ConnectedDevicesList(
    modifier: Modifier = Modifier,
    devices: List<Device>
) {
    LazyColumn(modifier) {
        items(devices.size) { index ->
            Text(
                text = devices[index].name,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatAppPreview() {
    ChatAppContent(
        DevicesPlaceholder.fakeDevices(),
        DevicesPlaceholder.fakeDevices(),
        {

        },
        {

        }
    )
}

@Preview(showBackground = true)
@Composable
fun NearbyDeviceTabPreview() {
    NearbyDeviceTab(DevicesPlaceholder.fakeDevices()) {

    }
}