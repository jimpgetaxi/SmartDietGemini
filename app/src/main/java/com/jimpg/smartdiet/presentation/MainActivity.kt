package com.jimpg.smartdiet.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jimpg.smartdiet.ui.theme.SmartDietGeminiTheme
import com.jimpg.smartdiet.SmartDietApp
import com.jimpg.smartdiet.presentation.home.HomeScreen
import com.jimpg.smartdiet.presentation.addmeal.AddMealScreen
import com.jimpg.smartdiet.presentation.profile.ProfileScreen
import com.jimpg.smartdiet.presentation.fasting.FastingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartDietGeminiTheme {
                val app = application as SmartDietApp
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            application = app,
                            onAddMealClick = {
                                navController.navigate("add_meal")
                            },
                            onProfileClick = {
                                navController.navigate("profile")
                            },
                            onFastingClick = {
                                navController.navigate("fasting")
                            }
                        )
                    }
                    composable("add_meal") {
                        AddMealScreen(
                            application = app,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            application = app,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("fasting") {
                        FastingScreen(
                            application = app,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

