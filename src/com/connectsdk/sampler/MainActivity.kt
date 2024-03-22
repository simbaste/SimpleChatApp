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
package com.connectsdk.sampler

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.device.DevicePicker
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManager.PairingLevel
import com.connectsdk.service.DeviceService
import com.connectsdk.service.DeviceService.PairingType
import com.connectsdk.service.capability.MediaPlayer
import com.connectsdk.service.command.ServiceCommandError
import com.samsung.multiscreen.Search
import com.samsung.multiscreen.Service

class MainActivity : AppCompatActivity(), ActionBar.TabListener {
    var mTV: ConnectableDevice? = null
    var dialog: AlertDialog? = null
    var pairingAlertDialog: AlertDialog? = null
    var pairingCodeDialog: AlertDialog? = null
    var dp: DevicePicker? = null
    var connectItem: MenuItem? = null
    var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mDiscoveryManager: DiscoveryManager? = null
    var mViewPager: ViewPager? = null
    var actionBar: ActionBar? = null

    private val deviceListener: ConnectableDeviceListener = object : ConnectableDeviceListener {
        override fun onPairingRequired(
            device: ConnectableDevice,
            service: DeviceService,
            pairingType: PairingType
        ) {
            Log.d("2ndScreenAPP", "Connected to " + mTV!!.ipAddress)
            when (pairingType) {
                PairingType.FIRST_SCREEN -> {
                    Log.d("2ndScreenAPP", "First Screen")
                    pairingAlertDialog!!.show()
                }

                PairingType.PIN_CODE, PairingType.MIXED -> {
                    Log.d("2ndScreenAPP", "Pin Code")
                    pairingCodeDialog!!.show()
                }

                PairingType.NONE -> {}
                else -> {}
            }
        }

        override fun onConnectionFailed(device: ConnectableDevice, error: ServiceCommandError) {
            Log.d("2ndScreenAPP", "onConnectFailed")
            connectFailed(mTV)
        }

        override fun onDeviceReady(device: ConnectableDevice) {
            Log.d("2ndScreenAPP", "onPairingSuccess")
            if (pairingAlertDialog!!.isShowing) {
                pairingAlertDialog!!.dismiss()
            }
            if (pairingCodeDialog!!.isShowing) {
                pairingCodeDialog!!.dismiss()
            }
            registerSuccess(mTV)
        }

        override fun onDeviceDisconnected(device: ConnectableDevice) {
            Log.d("2ndScreenAPP", "Device Disconnected")
            connectEnded(mTV)
            connectItem!!.setTitle("Connect")
            val frag = mSectionsPagerAdapter!!.getFragment(mViewPager!!.currentItem)
            if (frag != null) {
                Toast.makeText(applicationContext, "Device Disconnected", Toast.LENGTH_SHORT).show()
                frag.disableButtons()
            }
        }

        override fun onCapabilityUpdated(
            device: ConnectableDevice,
            added: List<String>,
            removed: List<String>
        ) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar = supportActionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        mSectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        mViewPager = findViewById<View>(R.id.pager) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val handler = Handler()
        handler.post {
            mViewPager!!.setOnPageChangeListener(object : SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    actionBar!!.setSelectedNavigationItem(position)
                }
            })
            for (i in 0 until mSectionsPagerAdapter!!.count) {
                actionBar!!.addTab(
                    actionBar!!.newTab()
                        .setIcon(mSectionsPagerAdapter!!.getIcon(i))
                        .setTabListener(this@MainActivity)
                )
            }
        }
        setupPicker()
        mDiscoveryManager = DiscoveryManager.getInstance()
        mDiscoveryManager?.registerDefaultDeviceTypes()
        mDiscoveryManager?.pairingLevel = PairingLevel.ON

        // To show all services in a device, a device item in DevicePickerList
        // mDiscoveryManager.setServiceIntegration(true);

        // To search devices with specific service types
        /*
        try {
            // AirPlay
            mDiscoveryManager.registerDeviceService((Class<DeviceService>) Class.forName("com.connectsdk.service.AirPlayService"),
                    (Class<DiscoveryProvider>)Class.forName("com.connectsdk.discovery.provider.ZeroconfDiscoveryProvider"));
            // webOS SSAP (Simple Service Access Protocol)
            mDiscoveryManager.registerDeviceService((Class<DeviceService>) Class.forName("com.connectsdk.service.WebOSTVService"),
                    (Class<DiscoveryProvider>)Class.forName("com.connectsdk.discovery.provider.SSDPDiscoveryProvider"));
            // DLNA
            mDiscoveryManager.registerDeviceService((Class<DeviceService>) Class.forName("com.connectsdk.service.DLNAService"),
                    (Class<DiscoveryProvider>)Class.forName("com.connectsdk.discovery.provider.SSDPDiscoveryProvider"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        */DiscoveryManager.getInstance().start()
    }

    val imageDevices: List<ConnectableDevice>
        get() {
            val imageDevices: MutableList<ConnectableDevice> = ArrayList()
            for (device in DiscoveryManager.getInstance().compatibleDevices.values) {
                if (device.hasCapability(MediaPlayer.Display_Image)) imageDevices.add(device)
            }
            return imageDevices
        }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null) {
            dialog!!.dismiss()
        }
        if (mTV != null) {
            mTV!!.disconnect()
        }
    }

    fun hConnectToggle() {
        if (!this.isFinishing) {
            if (mTV != null) {
                if (mTV!!.isConnected) mTV!!.disconnect()
                connectItem!!.setTitle("Connect")
                mTV!!.removeListener(deviceListener)
                mTV = null
                for (i in 0 until mSectionsPagerAdapter!!.count) {
                    if (mSectionsPagerAdapter!!.getFragment(i) != null) {
                        mSectionsPagerAdapter!!.getFragment(i).tv = null
                    }
                }
            } else {
                dialog!!.show()
            }
        }
    }

    private fun setupPicker() {
        dp = DevicePicker(this)
        dialog = dp!!.getPickerDialog("Device List") { arg0, arg1, arg2, arg3 ->
            mTV = arg0.getItemAtPosition(arg2) as ConnectableDevice
            mTV!!.addListener(deviceListener)
            mTV!!.setPairingType(null)
            mTV!!.connect()
            connectItem!!.setTitle(mTV!!.friendlyName)
            dp!!.pickDevice(mTV)
        }
        pairingAlertDialog = AlertDialog.Builder(this)
            .setTitle("Pairing with TV")
            .setMessage("Please confirm the connection on your TV")
            .setPositiveButton("Okay", null)
            .setNegativeButton("Cancel") { dialog, which ->
                dp!!.cancelPicker()
                hConnectToggle()
            }
            .create()
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        pairingCodeDialog = AlertDialog.Builder(this)
            .setTitle("Enter Pairing Code on TV")
            .setView(input)
            .setPositiveButton(android.R.string.ok) { arg0, arg1 ->
                if (mTV != null) {
                    val value = input.text.toString().trim { it <= ' ' }
                    mTV!!.sendPairingKey(value)
                    imm.hideSoftInputFromWindow(input.windowToken, 0)
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, whichButton ->
                dp!!.cancelPicker()
                hConnectToggle()
                imm.hideSoftInputFromWindow(input.windowToken, 0)
            }
            .create()
    }

    override fun onResume() {
        super.onResume()
    }

    fun registerSuccess(device: ConnectableDevice?) {
        Log.d("2ndScreenAPP", "successful register")
        val frag = mSectionsPagerAdapter!!.getFragment(mViewPager!!.currentItem)
        if (frag != null) frag.tv = mTV
    }

    fun connectFailed(device: ConnectableDevice?) {
        if (device != null) Log.d("2ndScreenAPP", "Failed to connect to " + device.ipAddress)
        if (mTV != null) {
            mTV!!.removeListener(deviceListener)
            mTV!!.disconnect()
            mTV = null
        }
    }

    fun connectEnded(device: ConnectableDevice?) {
        if (pairingAlertDialog!!.isShowing) {
            pairingAlertDialog!!.dismiss()
        }
        if (pairingCodeDialog!!.isShowing) {
            pairingCodeDialog!!.dismiss()
        }
        if (mTV!!.isConnecting == false) {
            mTV!!.removeListener(deviceListener)
            mTV = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        connectItem = menu.getItem(0)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_connect) {
            hConnectToggle()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}
    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}
    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        mViewPager!!.currentItem = tab.position
        title = mSectionsPagerAdapter!!.getTitle(tab.position)
        val frag = mSectionsPagerAdapter!!.getFragment(tab.position)
        if (frag != null) frag.tv = mTV
    }
}
