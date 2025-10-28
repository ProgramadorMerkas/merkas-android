package com.puntos.merkas.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puntos.merkas.R

enum class ErrorType {
    NONE,
    REQUIRED,
    INVALID_FORMAT
}

@Composable
fun TextField(
    // modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorType: ErrorType?,
    onFocusLost: (() -> Unit)?,
    placeholder: String,
    isPassword: Boolean = false,
    isRequired: Boolean = true,
    isEnabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next, // Por defecto, enter -> siguiente
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    forceShowError: Boolean = false
) {
    // Nuevo estado interno: detecta si se tocó el campo
    var hasBeenFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    // Determinar el tipo de teclado según el campo
    val keyboardType = when {
        label.contains(stringResource(id = R.string.phoneNumber), ignoreCase = true) -> KeyboardType.Number
        isPassword -> KeyboardType.Password
        label.contains(stringResource(id = R.string.email), ignoreCase = true) -> KeyboardType.Email
        else -> KeyboardType.Text
    }

    // Validaciones dinámicas
    val isValid = when {
        value.isBlank() -> false // obligatorio
        label.contains(stringResource(id = R.string.email), ignoreCase = true) -> {
            val emailRegex = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
            emailRegex.matches(value)
        }
        isPassword -> {
            val passwordRegex =
                Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
            passwordRegex.matches(value)
        }
        label.contains(stringResource(id = R.string.phoneNumber), ignoreCase = true) -> {
            value.all { it.isDigit() } && value.length >= 7
        }
        else -> true
    }

    val showError = (hasBeenFocused || forceShowError) && !isValid

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = {
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black, fontSize = 12.sp)) {
                        append(label)
                    }
                    if (isRequired) {withStyle(style = SpanStyle(colorResource(R.color.merkas))) {
                        append(" *")}
                    }
                })
            },
            // modifier = modifier,
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && hasBeenFocused) {
                        if (onFocusLost != null) {
                            onFocusLost()
                        }
                    } else if (focusState.isFocused) {
                        hasBeenFocused = true
                    }
                }
                .padding(top = 20.dp),
            placeholder = { Text(placeholder) },
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    onNext?.invoke() ?: focusManager.moveFocus(FocusDirection.Down)
                },
                onDone = {
                    onDone?.invoke() ?: focusManager.clearFocus()
                }
            ),
            trailingIcon = {
                when {
                isPassword -> {
                    val eyeImage = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = eyeImage, contentDescription = "Icono ocultar/mostrar contraseña")
                    }
                }

                    label.contains(stringResource(id = R.string.email), ignoreCase = true) -> {
                        if (isValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Email válido",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(15.dp),
            isError = showError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (hasBeenFocused && !isValid)
                    colorResource(id = R.color.merkas)
                else
                    Color.Black,
                unfocusedBorderColor = if (hasBeenFocused && !isValid)
                    colorResource(id = R.color.merkas)
                else Color.Gray,
                cursorColor = colorResource(id = R.color.merkas)
            ),
        )

        // Mensaje de error dinámico
        if (showError) {
            val errorMessage = when {
                value.isBlank() -> stringResource(id = R.string.requiredField)
                label.contains(stringResource(id=R.string.email), ignoreCase = true) ->
                    stringResource(id=R.string.validEmail)
                isPassword -> stringResource(id=R.string.validPassword)
                label.contains(stringResource(id=R.string.phoneNumber), ignoreCase = true) ->
                    stringResource(id=R.string.validPhoneNumber)
                else -> ""
            }
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.merkas),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}