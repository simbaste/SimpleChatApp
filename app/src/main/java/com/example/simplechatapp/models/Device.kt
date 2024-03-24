package com.example.simplechatapp.models

import com.samsung.multiscreen.Service

data class Device(
    val id: String,
    val name: String,
    val service: Service? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Device) return false
        return this.name == other.name && this.id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
