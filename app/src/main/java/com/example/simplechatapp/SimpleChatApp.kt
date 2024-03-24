package com.example.simplechatapp

import android.app.Application

class SimpleChatApp: Application() {

    companion object {
        lateinit var instance: SimpleChatApp
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}