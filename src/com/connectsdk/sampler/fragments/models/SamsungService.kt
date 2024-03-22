package com.connectsdk.sampler.fragments.models

import com.samsung.multiscreen.Service

data class SamsungService(
    val id: String,
    val content: String,
    var service: Service? = null
) {
    override fun toString(): String = content
}