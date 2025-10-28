package com.puntos.merkas.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puntos.merkas.R

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false,
)

fun validatePassword(password: String): PasswordValidationState {
    return PasswordValidationState(
        hasMinLength = password.length >= 8,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowerCase = password.any { it.isLowerCase() },
        hasNumber = password.any { it.isDigit() },
        hasSpecialChar = password.any { !it.isLetterOrDigit()}
    )
}

@Composable
fun PasswordRequirements(password: String) {
    val state = validatePassword(password)

    Column(modifier = Modifier.padding(top = 8.dp)) {
        RequirementItem("Al menos 8 caracteres", state.hasMinLength)
        RequirementItem("Una letra mayúscula", state.hasUppercase)
        RequirementItem("Una letra minúscula", state.hasLowerCase)
        RequirementItem("Un número", state.hasNumber)
        RequirementItem("Un caracter especial", state.hasSpecialChar)
    }
}

@Composable
fun RequirementItem(text: String, fulfilled: Boolean) {
    val color = if (fulfilled) Color(0xFF2E7D32) else colorResource(R.color.merkas)
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = if (fulfilled) "✓ $text" else "• $text",
            color = color,
            fontSize = 12.sp
        )
    }
}













