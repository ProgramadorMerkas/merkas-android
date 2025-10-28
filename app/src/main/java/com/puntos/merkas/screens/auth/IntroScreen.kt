package com.puntos.merkas.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puntos.merkas.R
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle

@Composable
fun IntroScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // IMAGEN CENTRAL -> LOGO
                Image(
                    painter = painterResource(id = R.drawable.a1024_1),
                    contentDescription = "Logo de la aplicación MERKAS",
                    modifier = Modifier.size(size = 280.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TEXTO INFERIOR DE LA IMAGEN CENTRAL
                Text(
                    text = stringResource(id = R.string.welcome_phrase),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BOTÓN INICIAR SESIÓN
            ButtonAuth(
                text = stringResource(R.string.login),
                style = ButtonAuthStyle.Login,
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(5.dp))

            // BOTÓN REGISTRARSE
            ButtonAuth(
                stringResource(R.string.signup),
                style = ButtonAuthStyle.Register,
                onClick = onRegisterClick
            )
        }
    }
}