package com.example.simplechatapp.viewmodel

import android.os.Bundle
import androidx.compose.ui.tooling.preview.Devices
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.simplechatapp.models.Device
import com.example.simplechatapp.models.DevicesPlaceholder
import com.example.simplechatapp.repository.SimpleChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SimpleChatViewModel(repository: SimpleChatRepository): ViewModel(), SimpleChatRepository by repository {

    companion object {
        fun provideFactory(
            repository: SimpleChatRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return SimpleChatViewModel(repository) as T
                }
            }
    }

}