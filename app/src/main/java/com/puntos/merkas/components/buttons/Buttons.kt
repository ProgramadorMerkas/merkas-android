package com.puntos.merkas.components.buttons

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.puntos.merkas.R

enum class ButtonAuthStyle {
    Login,
    Register
}

@Composable
fun ButtonAuth(
    text: String,
    onClick: () -> Unit,
    style: ButtonAuthStyle = ButtonAuthStyle.Login // Valor por defecto
) {
    val (containerColor, contentColor) = when (style) {
        ButtonAuthStyle.Login -> Triple(
            colorResource(id = R.color.merkas),
            Color.White,
            null
        )
        ButtonAuthStyle.Register -> Triple(
            Color.Transparent,
            colorResource(id = R.color.merkas),
            null
        )
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

@Composable
fun BackButton(navController: NavController) {
    var isNavigating by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (!isNavigating) {
                isNavigating = true
                navController.popBackStack()
            }
        },
        modifier = Modifier
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                Color(0xFFFFFFFF), RoundedCornerShape(120.dp)
            )
    ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                contentDescription = "Back",
                modifier = Modifier.padding(start = 8.dp)
            )
    }

    // Si quieres, puedes resetear el flag al volver a esta pantalla
    // con LaunchedEffect(navController) { isNavigating = false }

    BackHandler {
        if (!isNavigating) {
            isNavigating = true
            navController.popBackStack()
        }
    }
}
