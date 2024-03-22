package com.connectsdk.sampler.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.connectsdk.sampler.R
import com.connectsdk.sampler.fragments.models.SamsungService
import com.samsung.multiscreen.Application
import com.samsung.multiscreen.Channel
import com.samsung.multiscreen.Client
import com.samsung.multiscreen.Error
import com.samsung.multiscreen.Message
import com.samsung.multiscreen.Result
import com.samsung.multiscreen.Search
import com.samsung.multiscreen.Service

/**
 * A fragment representing a list of Items.
 */
class ItemFragment(context: Context?) : BaseFragment(context) {

    private lateinit var itemAdapter: MyItemRecyclerViewAdapter
    private lateinit var search: Search

    private val sendFreeMessageButton: Button? by lazy { requireView().findViewById(R.id.sendFreeMessage) }
    private val freeMessageEditText: EditText? by lazy { requireView().findViewById(R.id.freeMessageEditText) }

    private var application: Application? = null
    private var service: Service? = null
    private var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)

        // Set the adapter
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            itemAdapter = MyItemRecyclerViewAdapter { samsungService ->
                connectToService(samsungService)
            }
            adapter = itemAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search = Service.search(requireContext())

        println("create samsung search ==> $search")
        search.setOnServiceFoundListener { service ->
            println("==> Search found service: [$service]")
            itemAdapter.addService(SamsungService(
                id = service.id,
                content = service.name,
                service = service
            ))
        }

        search.setOnServiceLostListener { service ->
            println("==> Search lost service: [$service]")
        }

        println("start samsung search ==> ${search.start(true)}")

        sendFreeMessageButton?.setOnClickListener {
            val message = freeMessageEditText?.text?.toString() ?: "Default message"
            println("sending... $message to $service --> client = [$client]")
            val service = this.service ?: return@setOnClickListener
            val client = this.client ?: return@setOnClickListener
            sendMessage(service, client, message)
        }
    }

    private fun connectToService(samsungService: SamsungService) {
        println("connectToService called on $samsungService")

        service = samsungService.service ?: return
        // Example uri
        //http://www.lgappsampler.com.s3-website.eu-west-3.amazonaws.com/sampleApp?ip=172.17.17.58&port=8080
//        val url = Uri.parse("http://dev-multiscreen-examples.s3-website-us-west-1.amazonaws.com/examples/helloworld/tv/")

//        val url = Uri.parse("http://www.lgappsampler.com.s3-website.eu-west-3.amazonaws.com/sampleApp")
//        val url = Uri.parse("https://www.google.com")

        val url = Uri.parse("https://ntgrdb10.azureedge.net/main.html?ip=172.20.10.8&port=8080&debug")

        // Example channel id
        // Note: We recommend that you use a reverse domain style id for your
        // channel to prevent collisions.
//        val channelId = "com.samsung.multiscreen.helloworld"

        val channelId = "com.apple.sampleApp"

        // Get an instance of Application.
        application = service?.createApplication(url, channelId)

        // Listen for the connect event
        application?.setOnConnectListener { client ->
            println("Application.onConnect() client: $client")
            this.client = client
        }

        // Connect and launch the application.
        // When you connect to a service, the specified application will
        // be launched automatically.
        application?.connect(object : Result<Client> {
            override fun onSuccess(client: Client?) {
                println("Application connect onSuccess() client: $client")

                // The application is launched, and is ready to accept
                // messages.
            }

            override fun onError(error: Error?) {
                println("Application connect onError() error: $error")

                // Uh oh. Handle the error.
            }

        })

    }

    private fun sendMessage(service: Service, client: Client, message: String) {
        // Note: The TV application is designated as the "HOST"
        val application = this.application ?: return

        val event = "say"
        val messageData = message

        // Send a message to the TV application only, by default.
        application.publish(event, messageData)

        // Send a message to the TV application, explicitly.
        application.publish(event, messageData, Message.TARGET_HOST)

        // Send a "broadcast" message to all connected clients EXCEPT yourself.
        application.publish(event, messageData, Message.TARGET_BROADCAST)

        // Send a message to all clients INCLUDING yourself
        application.publish(event, messageData, Message.TARGET_ALL)

        // Example channel id
        // Note: We recommend that you use a reverse domain style id for your
        // channel to prevent collisions.
//        val channelId = "com.samsung.multiscreen.helloworld"
//        val channel = service.createChannel(service.uri)

        // Send a message to a specific client
//        val clientId = "123467" // Assuming that this is a valid id of a connected client
//        val client = channel.clients[clientId]
        application.publish(event, messageData, client)

        // Send a message to a list of clients
        val clients: List<Client> = listOf(/* You can create any combination list of clients */)
        application.publish(event, messageData, clients)

        // Send a binary message to the TV application only, by default.
        val payload = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        application.publish(event, messageData, payload)

    }

    override fun onDestroy() {
        super.onDestroy()
        search.stop()
    }
}