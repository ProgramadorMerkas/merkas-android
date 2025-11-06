package com.puntos.merkas.components.offers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun OffersCategory(
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        Modifier
            .width(300.dp)
            .height(45.dp)
            .padding(end = 7.dp)
            .background(Color.White, shape = CircleShape),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .width(300.dp)
                .height(45.dp)
                .background(Color.White, shape = CircleShape)
                .border(
                    width = 2.dp,
                    color = Color(0xFF3E69B4),
                    shape = CircleShape
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .background(Color(0xFF3E69B4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CellTower,
                    tint = Color.White,
                    contentDescription = ""
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center

            ) {
                Text(
                    "TECNOLOGÍA Y COMUNICACIONES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E69B4)
                )
            }
        }
    }
}