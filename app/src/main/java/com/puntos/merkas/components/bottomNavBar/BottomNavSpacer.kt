package com.puntos.merkas.components.bottomNavBar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavSpacer() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}