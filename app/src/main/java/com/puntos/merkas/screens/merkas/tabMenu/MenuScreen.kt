package com.puntos.merkas.screens.merkas.tabMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Hardware
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.puntos.merkas.R
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle
import com.puntos.merkas.data.services.SessionService
import com.puntos.merkas.data.services.TokenStore
import com.puntos.merkas.screens.merkas.tabHome.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(
    navController: NavHostController,
    parentNavController: NavController,
    tokenStore: TokenStore
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Handyman,
                    tint = colorResource(R.color.merkas),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(40.dp)
                )
                Text("Estamos trabajando para traer más funciones...",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        ButtonAuth(
            text = "Cerrar Sesión",
            onClick = {
                scope.launch {
                    SessionService.cerrarSesion(tokenStore)
                    parentNavController.popBackStack(route = "home", inclusive = true)
                    parentNavController.navigate("intro")
                }
            },
            style = ButtonAuthStyle.Login
        )
    }
}