package com.example.simplechatapp.repository

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import com.example.simplechatapp.SimpleChatApp
import com.example.simplechatapp.models.Device
import com.example.simplechatapp.server.MyWebSocketServer
import com.samsung.multiscreen.Client
import com.samsung.multiscreen.Error
import com.samsung.multiscreen.Result
import com.samsung.multiscreen.Search
import com.samsung.multiscreen.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class SimpleChatRepositoryImpl(val scope: CoroutineScope): SimpleChatRepository {

    companion object {
        val TAG: String = SimpleChatRepositoryImpl::class.java.simpleName
    }

    private val _connectedDevices = MutableSharedFlow<List<Device>>(1)
    override val connectedDevices: SharedFlow<List<Device>> = _connectedDevices.asSharedFlow()

    private val _nearbyDevices = MutableSharedFlow<List<Device>>(1)
    override val nearbyDevices: SharedFlow<List<Device>> = _nearbyDevices.asSharedFlow()

    private val _selectedDevice = MutableSharedFlow<Device>(1)
    override val selectedDevice: SharedFlow<Device> = _selectedDevice.asSharedFlow()

    private var myConnectedDevices = mutableSetOf<Device>()
    private var myNearbyDevices = mutableSetOf<Device>()

    private lateinit var search: Search

    private val ipAddress: String
        get() {
            val wifiManager =
                SimpleChatApp.instance.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        }

    private val webSocketServer = MyWebSocketServer.startServer(
        ipAddress,
        8080,
        object : MyWebSocketServer.Listener {
            override fun onNewConnection(address: InetSocketAddress) {
                scope.launch {
                    myConnectedDevices.add(Device(
                        id = address.toString(),
                        name = address.address?.hostAddress ?: "---"
                    ))
                    _connectedDevices.emit(myConnectedDevices.toList())
                }
            }

            override fun onConnectionClosed(address: InetSocketAddress) {
                scope.launch {
                    myConnectedDevices.remove(
                        Device(
                            id = address.toString(),
                            name = address.address?.hostAddress ?: "---"
                    ))
                    _connectedDevices.emit(myConnectedDevices.toList())
                }
            }
        }
    )

    override fun selectDevice(device: Device) {
        scope.launch {
            _selectedDevice.emit(device)
        }
    }

    override fun openHelloWorldApp() {
        Log.d(TAG, "openHelloWorldApp() called: selectedDevice = [${selectedDevice.replayCache.lastOrNull()}]")
        val service = selectedDevice.replayCache.lastOrNull()?.service ?: return
        val url = Uri.parse("http://dev-multiscreen-examples.s3-website-us-west-1.amazonaws.com/examples/helloworld/tv/")
        val channelId = "com.google.sampleApp"
        connectToApplication(service, url, channelId)
    }

    override fun openGoogleApp() {
        Log.d(TAG, "openGoogleApp() called: selectedDevice = [${selectedDevice.replayCache.lastOrNull()}]")
        val service = selectedDevice.replayCache.lastOrNull()?.service ?: return
        val url = Uri.parse("https://www.google.com")
        val channelId = "com.helloWorld.sampleApp"
        connectToApplication(service, url, channelId)
    }

    private fun connectToApplication(
        service: Service,
        url: Uri?,
        channelId: String
    ) {
        Log.d(TAG, "connectToApplication() called with: service = [$service], uri = [$url], channelId = [$channelId]")
        // Get an instance of Application.
        val application = service.createApplication(url, channelId)

        // Listen for the connect event
        application?.setOnConnectListener { client ->
            Log.d(TAG, "Application.onConnect() client: $client")
        }

        // Connect and launch the application.
        // When you connect to a service, the specified application will
        // be launched automatically.
        application?.connect(object : Result<Client> {
            override fun onSuccess(client: Client?) {
                Log.d(TAG, "Application connect onSuccess() client: $client")

                // The application is launched, and is ready to accept
                // messages.
            }

            override fun onError(error: Error?) {
                Log.d(TAG, "Application connect onError() error: $error")
                // Uh oh. Handle the error.
            }
        })
    }

    override fun sendMessage(message: String) {
        webSocketServer.sendMessage(message)
    }

    override fun search(context: Context) {
        search = Service.search(context)
        search.setOnServiceFoundListener { service ->
            Log.d(TAG, "Search found service: [$service]")
            scope.launch {
                myNearbyDevices.add(Device(
                    id = service.id,
                    name = service.name,
                    service = service
                ))
                _nearbyDevices.emit(myNearbyDevices.toList())
            }
        }

        search.setOnServiceLostListener { service ->
            Log.d(TAG, "Search lost service: [$service]")
            scope.launch {
                myNearbyDevices.remove(Device(
                    id = service.id,
                    name = service.name,
                    service = service
                ))
                _nearbyDevices.emit(myNearbyDevices.toList())
            }
        }

        Log.d(TAG, "start samsung search ${search.start(true)}")
    }

}