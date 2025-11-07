package com.puntos.merkas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.puntos.merkas.components.bottomNavBar.MainNavigation
import com.puntos.merkas.data.services.TokenStore
import com.puntos.merkas.screens.auth.login.LoginScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenStore = TokenStore(this)

        lifecycleScope.launch {
            val token = tokenStore.getToken()
            val isLoggedIn = !token.isNullOrEmpty()

            setContent {
                MainNavigation(isLoggedIn = isLoggedIn)
            }
        }

//        setContent {
//              MainNavigation()
//      }
    }
}

