package com.puntos.merkas.components.bottomNavBar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeNavigation() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)

            // Este NavGraph es el interno, el que define las pantallas de navegación del BottomNav
            // (inicio, aliados, ofertas, mis referidos, menú)
        )
    }
}