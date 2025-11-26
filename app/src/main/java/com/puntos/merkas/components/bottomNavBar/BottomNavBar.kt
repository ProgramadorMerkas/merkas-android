package com.puntos.merkas.components.bottomNavBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.puntosmerkas.co.R

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 30.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(24.dp)),
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route) {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },

                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            item.label,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(R.color.merkas),
                        selectedTextColor = colorResource(R.color.merkas),
                        unselectedIconColor = colorResource(R.color.title),
                        unselectedTextColor = colorResource(R.color.title),
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}