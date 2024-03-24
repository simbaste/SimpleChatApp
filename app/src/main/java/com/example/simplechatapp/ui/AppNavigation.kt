package com.example.simplechatapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.simplechatapp.viewmodel.SimpleChatViewModel

@Composable
fun AppNavigation(viewModel: SimpleChatViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            ChatApp(viewModel = viewModel, navController = navController)
        }
        composable("device/{deviceName}") { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName")
            deviceName?.let { DeviceDetailsScreen(viewModel = viewModel, deviceName = deviceName) }
        }
    }
}