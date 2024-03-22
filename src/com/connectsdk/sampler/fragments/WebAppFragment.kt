//
//  Connect SDK Sample App by LG Electronics
//
//  To the extent possible under law, the person who associated CC0 with
//  this sample app has waived all copyright and related or neighboring rights
//  to the sample app.
//
//  You should have received a copy of the CC0 legalcode along with this
//  work. If not, see http://creativecommons.org/publicdomain/zero/1.0/.
//
package com.connectsdk.sampler.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.connectsdk.sampler.R
import com.connectsdk.sampler.server.MyWebSocketServer
import com.connectsdk.sampler.util.TestResponseObject
import com.connectsdk.service.capability.WebAppLauncher
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.command.ServiceSubscription
import com.connectsdk.service.sessions.LaunchSession
import com.connectsdk.service.sessions.WebAppSession
import com.connectsdk.service.sessions.WebAppSession.WebAppPinStatusListener
import com.connectsdk.service.sessions.WebAppSessionListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class WebAppFragment : BaseFragment {
    var launchWebAppButton: Button? = null
    var joinWebAppButton: Button? = null
    var leaveWebAppButton: Button? = null
    var closeWebAppButton: Button? = null
    var sendMessageButton: Button? = null
    var sendJSONButton: Button? = null
    var pinWebAppButton: Button? = null
    var unPinWebAppButton: Button? = null
    var testResponse: TestResponseObject? = null
    var responseMessageTextView: TextView? = null
    var runningAppSession: LaunchSession? = null
    var mWebAppSession: WebAppSession? = null
    var isWebAppPinnedSubscription: ServiceSubscription<WebAppPinStatusListener>? = null
    var webAppId: String? = null

    val webSocketServer = MyWebSocketServer.startServer()

    private val sendFreeMessageButton: Button? by lazy { requireView().findViewById(R.id.sendFreeMessage) }
    private val freeMessageEditText: EditText? by lazy { requireView().findViewById(R.id.freeMessageEditText) }
    constructor(context: Context?) : super(context) {
        testResponse = TestResponseObject()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        val rootView = inflater.inflate(
            R.layout.fragment_webapp, container, false
        )
        launchWebAppButton = rootView.findViewById<View>(R.id.launchWebAppButton) as Button
        joinWebAppButton = rootView.findViewById<View>(R.id.joinWebAppButton) as Button
        leaveWebAppButton = rootView.findViewById<View>(R.id.leaveWebAppButton) as Button
        closeWebAppButton = rootView.findViewById<View>(R.id.closeWebAppButton) as Button
        sendMessageButton = rootView.findViewById<View>(R.id.sendMessageButton) as Button
        sendJSONButton = rootView.findViewById<View>(R.id.sendJSONButton) as Button
        responseMessageTextView =
            rootView.findViewById<View>(R.id.responseMessageTextView) as TextView
        pinWebAppButton = rootView.findViewById<View>(R.id.pinWebAppButton) as Button
        unPinWebAppButton = rootView.findViewById<View>(R.id.unPinWebAppButton) as Button
        buttons = arrayOf(
            launchWebAppButton,
            joinWebAppButton,
            leaveWebAppButton,
            closeWebAppButton,
            sendMessageButton,
            sendJSONButton,
            pinWebAppButton,
            unPinWebAppButton
        )
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendFreeMessageButton?.setOnClickListener {
            val message = freeMessageEditText?.text?.toString() ?: "Default message"
            println("sending... $message")
            webSocketServer.sendMessage(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketServer.stop()
    }

    override fun enableButtons() {
        super.enableButtons()
        if (tv.hasCapability(WebAppLauncher.Launch)) {
            launchWebAppButton!!.setOnClickListener(launchWebApp)
        } else {
            disableButton(launchWebAppButton)
        }
        joinWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Launch)
        joinWebAppButton!!.setOnClickListener(joinWebApp)
        leaveWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Disconnect)
        leaveWebAppButton!!.setOnClickListener(leaveWebApp)
        if (tv.hasCapability(WebAppLauncher.Close)) {
            closeWebAppButton!!.setOnClickListener(closeWebApp)
        }
        if (tv.hasCapability(WebAppLauncher.Message_Send)) {
            sendMessageButton!!.setOnClickListener(sendMessage)
            sendJSONButton!!.setOnClickListener(sendJson)
        }
        if (tv.hasCapability(WebAppLauncher.Pin)) {
            pinWebAppButton!!.setOnClickListener(pinWebApp)
            unPinWebAppButton!!.setOnClickListener(unPinWebApp)
        }
        responseMessageTextView!!.text = ""
        if (!isLaunched) {
            disableButton(closeWebAppButton)
            disableButton(leaveWebAppButton)
            disableButton(sendMessageButton)
            disableButton(sendJSONButton)
        } else {
            disableButton(launchWebAppButton)
        }
        if (tv.getServiceByName(WEBOSID) != null) {
            webAppId = "SampleWebApp"
        } else if (tv.getServiceByName(CASTID) != null) {
            webAppId = "DDCEDE96"
        } else if (tv.getServiceByName(MULTISCREENID) != null) {
            webAppId = "ConnectSDKSampler"
        }
        if (tv.hasCapability(WebAppLauncher.Pin)) {
            subscribeIfWebAppIsPinned()
        } else {
            disableButton(pinWebAppButton)
            disableButton(unPinWebAppButton)
        }
    }

//    val webSiteUrl = "https://ntgrdb6.azureedge.net/index.html"
    val webSiteUrl = "http://www.lgappsampler.com.s3-website.eu-west-3.amazonaws.com/sampleApp?ip=172.17.17.58&port=8080"

    var launchWebApp = View.OnClickListener {
        if (webAppId == null) return@OnClickListener
        launchWebAppButton!!.isEnabled = false
        webAppLauncher.launchBrowser(
            webSiteUrl,
            object : WebAppSession.LaunchListener {
                override fun onError(error: ServiceCommandError) {
                    Log.e("LG", "Error connecting to web app | error = $error")
                    launchWebAppButton!!.isEnabled = true
                }

                override fun onSuccess(webAppSession: WebAppSession) {
                    println("Web App successfully launch on tv = [$tv], with webAppSession = [$webAppSession]")
                    testResponse = TestResponseObject(
                        true,
                        TestResponseObject.SuccessCode,
                        TestResponseObject.Launched_WebAPP
                    )
                    webAppSession.webAppSessionListener = webAppListener
                    isLaunched = true
                    if (tv.hasAnyCapability(
                            WebAppLauncher.Message_Send,
                            WebAppLauncher.Message_Receive,
                            WebAppLauncher.Message_Receive_JSON,
                            WebAppLauncher.Message_Send_JSON
                        )
                    ) {
                        println("TV has messages capabilities ==>")
                        webAppSession.connect(connectionListener)
                    } else {
                        println("TV doesn't have messages capabilities ==>")
                        connectionListener.onSuccess(
                            webAppSession.launchSession
                        )
                    }
                    mWebAppSession = webAppSession
                }
            })

//            getWebAppLauncher().launchWebApp(webAppId, new LaunchListener() {
//
//                @Override
//                public void onError(ServiceCommandError error) {
//                    Log.e("LG", "Error connecting to web app | error = " + error);
//                    launchWebAppButton.setEnabled(true);
//                }
//
//                @Override
//                public void onSuccess(WebAppSession webAppSession) {
//                    testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Launched_WebAPP);
//                    webAppSession.setWebAppSessionListener(webAppListener);
//                    isLaunched = true;
//
//                    if (getTv().hasAnyCapability(WebAppLauncher.Message_Send, WebAppLauncher.Message_Receive, WebAppLauncher.Message_Receive_JSON, WebAppLauncher.Message_Send_JSON))
//                        webAppSession.connect(connectionListener);
//                    else
//                        connectionListener.onSuccess(webAppSession.launchSession);
//
//                    mWebAppSession = webAppSession;
//                }
//            });
    }
    private var joinWebApp = View.OnClickListener {
        if (webAppId == null) return@OnClickListener
        webAppLauncher.joinWebApp(webAppId, object : WebAppSession.LaunchListener {
            override fun onError(error: ServiceCommandError) {
                Log.d("LG", "Could not join")
            }

            override fun onSuccess(webAppSession: WebAppSession) {
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.Joined_WebAPP
                )
                if (tv == null) return
                webAppSession.webAppSessionListener = webAppListener
                mWebAppSession = webAppSession
                sendMessageButton!!.isEnabled = true
                launchWebAppButton!!.isEnabled = false
                leaveWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Disconnect)
                if (tv.hasCapabilities(WebAppLauncher.Message_Send_JSON)) sendJSONButton!!.isEnabled =
                    true
                if (tv.hasCapabilities(WebAppLauncher.Close)) closeWebAppButton!!.isEnabled = true
                isLaunched = true
            }
        })
    }
    var leaveWebApp = View.OnClickListener {
        if (mWebAppSession != null) {
            mWebAppSession!!.webAppSessionListener = null
            mWebAppSession!!.disconnectFromWebApp()
            mWebAppSession = null
            launchWebAppButton!!.isEnabled = true
            joinWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Join)
            sendMessageButton!!.isEnabled = false
            sendJSONButton!!.isEnabled = false
            leaveWebAppButton!!.isEnabled = false
            closeWebAppButton!!.isEnabled = false
            isLaunched = false
        }
    }
    var pinWebApp = View.OnClickListener {
        if (tv != null) {
            webAppLauncher.pinWebApp(webAppId, object : ResponseListener<Any?> {
                override fun onError(error: ServiceCommandError) {
                    Log.w(TAG, "pin web app failure, " + error.localizedMessage)
                }

                override fun onSuccess(`object`: Any?) {
                    testResponse = TestResponseObject(
                        true,
                        TestResponseObject.SuccessCode,
                        TestResponseObject.Pinned_WebAPP
                    )
                    Log.d(TAG, "pin web app success")
                }
            })
        }
    }
    var unPinWebApp = View.OnClickListener {
        if (webAppId == null) return@OnClickListener
        if (tv != null) {
            webAppLauncher.unPinWebApp(webAppId, object : ResponseListener<Any?> {
                override fun onError(error: ServiceCommandError) {
                    Log.w(TAG, "unpin web app failture, " + error.localizedMessage)
                }

                override fun onSuccess(`object`: Any?) {
                    testResponse = TestResponseObject(
                        true,
                        TestResponseObject.SuccessCode,
                        TestResponseObject.UnPinned_WebAPP
                    )
                    Log.d(TAG, "unpin web app success")
                }
            })
        }
    }

    fun checkIfWebAppIsPinned() {
        if (webAppId == null) return
        webAppLauncher.isWebAppPinned(webAppId, object : WebAppPinStatusListener {
            override fun onError(error: ServiceCommandError) {
                Log.w(TAG, "isWebAppPinned failture, " + error.localizedMessage)
            }

            override fun onSuccess(status: Boolean) {
                updatePinButton(status)
            }
        })
    }

    fun subscribeIfWebAppIsPinned() {
        if (webAppId == null) return
        isWebAppPinnedSubscription =
            webAppLauncher.subscribeIsWebAppPinned(webAppId, object : WebAppPinStatusListener {
                override fun onError(error: ServiceCommandError) {
                    Log.w(TAG, "isWebAppPinned failure, " + error.localizedMessage)
                }

                override fun onSuccess(status: Boolean) {
                    updatePinButton(status)
                }
            })
    }

    fun updatePinButton(status: Boolean) {
        if (status) {
            pinWebAppButton!!.isEnabled = false
            unPinWebAppButton!!.isEnabled = true
        } else {
            pinWebAppButton!!.isEnabled = true
            unPinWebAppButton!!.isEnabled = false
        }
    }

    var webAppListener: WebAppSessionListener = object : WebAppSessionListener {
        override fun onReceiveMessage(webAppSession: WebAppSession, message: Any) {
            Log.d(TAG, "Message received from app | $message")
            if (message.javaClass == String::class.java) {
                responseMessageTextView!!.append(message as String)
                responseMessageTextView!!.append("\n")
            } else if (message.javaClass == JSONObject::class.java) {
                responseMessageTextView!!.append((message as JSONObject).toString())
                responseMessageTextView!!.append("\n")
            }
        }

        override fun onWebAppSessionDisconnect(webAppSession: WebAppSession) {
            Log.d("LG", "Device was disconnected")
            if (webAppSession !== mWebAppSession) {
                webAppSession.webAppSessionListener = null
                return
            }
            launchWebAppButton!!.isEnabled = true
            if (tv != null) joinWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Join)
            sendMessageButton!!.isEnabled = false
            sendJSONButton!!.isEnabled = false
            leaveWebAppButton!!.isEnabled = false
            closeWebAppButton!!.isEnabled = false
            mWebAppSession!!.webAppSessionListener = null
            mWebAppSession = null
            isLaunched = false
        }
    }
    var connectionListener: ResponseListener<Any?> = object : ResponseListener<Any?> {
        override fun onSuccess(response: Any?) {
            println("SUCCESSFULLY CONNECT TO TV = [$response]")
            if (tv == null) return
            if (tv.hasCapability(WebAppLauncher.Message_Send_JSON)) sendJSONButton!!.isEnabled =
                true
            if (tv.hasCapability(WebAppLauncher.Message_Send)) sendMessageButton!!.isEnabled = true
            leaveWebAppButton!!.isEnabled = tv.hasCapability(WebAppLauncher.Disconnect)
            closeWebAppButton!!.isEnabled = true
            launchWebAppButton!!.isEnabled = false
            isLaunched = true
        }

        override fun onError(error: ServiceCommandError) {
            println("CONNECTION TO TV FAILED = [$error]")
            sendJSONButton!!.isEnabled = false
            sendMessageButton!!.isEnabled = false
            closeWebAppButton!!.isEnabled = false
            launchWebAppButton!!.isEnabled = true
            isLaunched = false
            if (mWebAppSession != null) {
                mWebAppSession!!.webAppSessionListener = null
                mWebAppSession!!.close(null)
            }
        }
    }
    var sendMessage = View.OnClickListener {
        val message = "This is an Android test message."
        mWebAppSession!!.sendMessage(message, object : ResponseListener<Any?> {
            @SuppressLint("SuspiciousIndentation")
            override fun onSuccess(response: Any?) {
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.Sent_Message
                )
                Log.d(TAG, "Sent message : $response")
            }

            override fun onError(error: ServiceCommandError) {
                Log.e(TAG, "Error sending message : $error")
            }
        })
    }
    var sendJson = View.OnClickListener {
        var message: JSONObject? = null
        message = try {
            object : JSONObject() {
                init {
                    put("type", "message")
                    put("contents", "This is a test message")
                    put("params", object : JSONObject() {
                        init {
                            put(
                                "someParam1",
                                "The content & format of this JSON block can be anything"
                            )
                            put("someParam2", "The only limit ... is yourself")
                        }
                    })
                    put("anArray", object : JSONArray() {
                        init {
                            put("Just")
                            put("to")
                            put("show")
                            put("we")
                            put("can")
                            put("send")
                            put("arrays!")
                        }
                    })
                }
            }
        } catch (e: JSONException) {
            return@OnClickListener
        }
        mWebAppSession!!.sendMessage(message, object : ResponseListener<Any?> {
            override fun onSuccess(response: Any?) {
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.Sent_JSON
                )
                Log.d(TAG, "Sent message : $response")
            }

            override fun onError(error: ServiceCommandError) {
                Log.e(TAG, "Error sending message : $error")
            }
        })
    }
    var closeWebApp = View.OnClickListener {
        responseMessageTextView!!.text = ""
        closeWebAppButton!!.isEnabled = false
        sendMessageButton!!.isEnabled = false
        sendJSONButton!!.isEnabled = false
        leaveWebAppButton!!.isEnabled = false
        isLaunched = false
        mWebAppSession!!.webAppSessionListener = null
        mWebAppSession!!.close(object : ResponseListener<Any?> {
            override fun onSuccess(response: Any?) {
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.Close_WebAPP
                )
                launchWebAppButton!!.isEnabled = true
            }

            override fun onError(error: ServiceCommandError) {
                Log.e(TAG, "Error closing web app | error = $error")
                launchWebAppButton!!.isEnabled = true
            }
        })
    }

    override fun disableButtons() {
        super.disableButtons()
        isLaunched = false
        responseMessageTextView!!.text = ""
        webAppId = null
    }

    fun setRunningAppInfo(session: LaunchSession?) {
        runningAppSession = session
    }

    companion object {
        const val TAG = "Connect SDK"
        private const val WEBOSID = "webOS TV"
        private const val CASTID = "Chromecast"
        private const val MULTISCREENID = "MultiScreen"
        var isLaunched = false
    }
}