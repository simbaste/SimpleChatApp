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

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.connectsdk.core.ExternalInputInfo
import com.connectsdk.sampler.R
import com.connectsdk.sampler.util.TestResponseObject
import com.connectsdk.service.capability.ExternalInputControl
import com.connectsdk.service.capability.ExternalInputControl.ExternalInputListListener
import com.connectsdk.service.capability.Launcher.AppLaunchListener
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.capability.VolumeControl.MuteListener
import com.connectsdk.service.capability.VolumeControl.VolumeListener
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.command.ServiceSubscription
import com.connectsdk.service.sessions.LaunchSession

class SystemFragment : BaseFragment {
    var muteToggleButton: CheckBox? = null
    var volumeUpButton: Button? = null
    var volumeDownButton: Button? = null
    var volumeSlider: SeekBar? = null
    var playButton: Button? = null
    var pauseButton: Button? = null
    var stopButton: Button? = null
    var rewindButton: Button? = null
    var fastForwardButton: Button? = null
    var inputPickerButton: Button? = null
    var inputListView: ListView? = null
    var adapter: ArrayAdapter<String>? = null
    var inputPickerSession: LaunchSession? = null
    var testResponse: TestResponseObject? = null
    private var mVolumeSubscription: ServiceSubscription<VolumeListener>? = null
    private var mMuteSubscription: ServiceSubscription<MuteListener>? = null

    constructor()
    constructor(context: Context?) : super(context) {
        testResponse = TestResponseObject()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.fragment_system, container, false
        )
        muteToggleButton = rootView.findViewById<View>(R.id.muteToggle) as CheckBox
        volumeUpButton = rootView.findViewById<View>(R.id.volumeUpButton) as Button
        volumeDownButton = rootView.findViewById<View>(R.id.volumeDownButton) as Button
        inputListView = rootView.findViewById<View>(R.id.inputListView) as ListView
        playButton = rootView.findViewById<View>(R.id.playButton) as Button
        pauseButton = rootView.findViewById<View>(R.id.pauseButton) as Button
        stopButton = rootView.findViewById<View>(R.id.stopButton) as Button
        rewindButton = rootView.findViewById<View>(R.id.rewindButton) as Button
        fastForwardButton = rootView.findViewById<View>(R.id.fastForwardButton) as Button
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        inputListView!!.adapter = adapter
        volumeSlider = rootView.findViewById<View>(R.id.volumeSlider) as SeekBar
        volumeSlider!!.max = 100
        inputListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val input = arg0.getItemAtPosition(arg2) as String
            if (tv.hasCapability(ExternalInputControl.Set)) {
                val inputInfo = ExternalInputInfo()
                inputInfo.id = input
                externalInputControl.setExternalInput(inputInfo, null)
            }
        }
        inputPickerButton = rootView.findViewById<View>(R.id.inputPickerButton) as Button
        buttons = arrayOf(
            inputPickerButton,
            volumeUpButton,
            volumeDownButton,
            muteToggleButton,
            pauseButton,
            playButton,
            stopButton,
            rewindButton,
            fastForwardButton
        )
        buttons[0] = inputPickerButton
        return rootView
    }

    override fun enableButtons() {
        super.enableButtons()
        if (tv.hasCapability(ExternalInputControl.List)) externalInputControl.getExternalInputList(
            externalInputListener
        )
        volumeSlider!!.isEnabled = tv.hasCapability(VolumeControl.Volume_Set)
        inputPickerButton!!.isEnabled = tv.hasCapability(ExternalInputControl.Picker_Launch)
        muteToggleButton!!.isEnabled = tv.hasCapability(VolumeControl.Mute_Set)
        volumeUpButton!!.isEnabled = tv.hasCapability(VolumeControl.Volume_Up_Down)
        volumeDownButton!!.isEnabled = tv.hasCapability(VolumeControl.Volume_Up_Down)
        playButton!!.isEnabled = tv.hasCapability(MediaControl.Play)
        pauseButton!!.isEnabled = tv.hasCapability(MediaControl.Pause)
        stopButton!!.isEnabled = tv.hasCapability(MediaControl.Stop)
        rewindButton!!.isEnabled = tv.hasCapability(MediaControl.Rewind)
        fastForwardButton!!.isEnabled = tv.hasCapability(MediaControl.FastForward)
        if (tv.hasCapability(VolumeControl.Volume_Subscribe)) mVolumeSubscription =
            volumeControl.subscribeVolume(volumeListener)
        if (tv.hasCapability(VolumeControl.Mute_Subscribe)) mMuteSubscription =
            volumeControl.subscribeMute(muteListener)
        inputPickerButton!!.setOnClickListener(inputPickerClickListener)
        volumeUpButton!!.setOnClickListener(volumeChangedClickListener)
        volumeDownButton!!.setOnClickListener(volumeChangedClickListener)
        muteToggleButton!!.setOnClickListener(muteToggleClickListener)
        volumeSlider!!.setOnSeekBarChangeListener(volumeSeekListener)
        playButton!!.setOnClickListener(playClickListener)
        pauseButton!!.setOnClickListener(pauseClickListener)
        stopButton!!.setOnClickListener(stopClickListener)
        rewindButton!!.setOnClickListener(rewindClickListener)
        fastForwardButton!!.setOnClickListener(fastForwardClickListener)
    }

    private val muteToggleClickListener = View.OnClickListener {
        volumeControl.setMute(muteToggleButton!!.isChecked, null)
        if (muteToggleButton!!.isChecked) {
            testResponse = TestResponseObject(
                true,
                TestResponseObject.SuccessCode,
                TestResponseObject.Muted_Media
            )
        } else if (!muteToggleButton!!.isChecked) {
            testResponse = TestResponseObject(
                true,
                TestResponseObject.SuccessCode,
                TestResponseObject.UnMuted_Media
            )
        }
    }
    private val volumeChangedClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.volumeDownButton -> {
                volumeControl.volumeDown(null)
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.VolumeDown
                )
            }

            R.id.volumeUpButton -> {
                volumeControl.volumeUp(null)
                testResponse = TestResponseObject(
                    true,
                    TestResponseObject.SuccessCode,
                    TestResponseObject.VolumeUp
                )
            }
        }
    }
    private val inputPickerClickListener = View.OnClickListener {
        if (inputPickerButton!!.isSelected) {
            if (inputPickerSession != null) {
                inputPickerButton!!.isSelected = false
                externalInputControl.closeInputPicker(inputPickerSession, null)
            }
        } else {
            inputPickerButton!!.isSelected = true
            if (externalInputControl != null) {
                externalInputControl.launchInputPicker(object : AppLaunchListener {
                    override fun onError(error: ServiceCommandError) {}
                    override fun onSuccess(`object`: LaunchSession) {
                        inputPickerSession = `object`
                        testResponse = TestResponseObject(
                            true,
                            TestResponseObject.SuccessCode,
                            TestResponseObject.InputPickerVisible
                        )
                    }
                })
            }
        }
    }
    private val externalInputListener: ExternalInputListListener =
        object : ExternalInputListListener {
            override fun onSuccess(externalInputList: List<ExternalInputInfo>) {
                adapter!!.clear()
                for (i in externalInputList.indices) {
                    val input = externalInputList[i]
                    val deviceId = input.id
                    adapter!!.add(deviceId)
                }
            }

            override fun onError(arg0: ServiceCommandError) {}
        }
    private val volumeListener: VolumeListener = object : VolumeListener {
        override fun onSuccess(volume: Float) {
            volumeSlider!!.progress = (volume * 100).toInt()
        }

        override fun onError(error: ServiceCommandError) {
            Log.d("LG", "Error subscribing to volume: $error")
        }
    }
    private val muteListener: MuteListener = object : MuteListener {
        override fun onSuccess(`object`: Boolean) {
            muteToggleButton!!.isChecked = `object`
        }

        override fun onError(error: ServiceCommandError) {
            Log.d("LG", "Error subscribing to mute: $error")
        }
    }
    private val volumeSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val fVol = (progress / 100.0).toFloat()
                volumeControl.setVolume(fVol, null)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }
    private val playClickListener = View.OnClickListener { mediaControl.play(null) }
    private val pauseClickListener = View.OnClickListener { mediaControl.pause(null) }
    private val stopClickListener = View.OnClickListener { mediaControl.stop(null) }
    private val rewindClickListener = View.OnClickListener { mediaControl.rewind(null) }
    private val fastForwardClickListener = View.OnClickListener { mediaControl.fastForward(null) }
    override fun disableButtons() {
        adapter!!.clear()
        volumeSlider!!.isEnabled = false
        volumeSlider!!.setOnSeekBarChangeListener(null)
        if (mVolumeSubscription != null) {
            mVolumeSubscription!!.unsubscribe()
            mVolumeSubscription = null
        }
        if (mMuteSubscription != null) {
            mMuteSubscription!!.unsubscribe()
            mMuteSubscription = null
        }
        super.disableButtons()
    }
}
