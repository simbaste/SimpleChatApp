package com.example.simplechatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.simplechatapp.repository.SimpleChatRepositoryImpl
import com.example.simplechatapp.ui.AppNavigation
import com.example.simplechatapp.ui.ChatApp
import com.example.simplechatapp.ui.theme.SimpleChatAppTheme
import com.example.simplechatapp.viewmodel.SimpleChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SimpleChatViewModel by viewModels {
        SimpleChatViewModel.provideFactory(SimpleChatRepositoryImpl(CoroutineScope(Dispatchers.IO)), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.search(this@MainActivity)
            }
        }
        setContent {
            SimpleChatAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}