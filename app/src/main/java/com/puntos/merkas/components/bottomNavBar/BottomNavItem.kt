package com.puntos.merkas.components.bottomNavBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Default.House, "Inicio"),
    BottomNavItem("allies", Icons.Default.PinDrop, "Aliados"),
    BottomNavItem("offers", Icons.Default.LocalOffer, "Ofertas"),
    BottomNavItem("referrals", Icons.Default.Groups, "Referidos"),
    BottomNavItem("menu", Icons.Default.Menu, "Menú"),
)
