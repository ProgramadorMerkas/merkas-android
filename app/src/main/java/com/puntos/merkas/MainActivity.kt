package com.puntos.merkas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.puntos.merkas.components.bottomNavBar.HomeNavigation
import com.puntos.merkas.components.transitions.composableWithTransitions
import com.puntos.merkas.ui.theme.Merkas_kotlinTheme
import com.puntos.merkas.screens.auth.IntroScreen
import com.puntos.merkas.screens.auth.login.LoginScreen
import com.puntos.merkas.screens.auth.signup.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                MainNavigation()
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "intro"
    ) {
        composableWithTransitions("intro") {
            IntroScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("signup") }
            )
        }
        composableWithTransitions("login") {
            LoginScreen(
                navController = navController,
                homeScreen = { navController.navigate("home") {
                    popUpTo("intro") { inclusive = true }
                } }
            )
        }
        composableWithTransitions("signup") {
            SignUpScreen(
                navController = navController,
                homeScreen = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composableWithTransitions("home") {
            HomeNavigation()
        }
    }
}