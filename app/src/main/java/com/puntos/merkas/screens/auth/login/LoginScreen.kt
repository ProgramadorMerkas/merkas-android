package com.puntos.merkas.screens.auth.login

import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.puntos.merkas.R
import com.puntos.merkas.auth.AuthViewModel
import com.puntos.merkas.auth.AuthViewModelFactory
import com.puntos.merkas.components.buttons.BackButton
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle
import com.puntos.merkas.components.inputs.ErrorType
import com.puntos.merkas.components.inputs.TextField
import com.puntos.merkas.components.loaders.Loading
import com.puntos.merkas.screens.merkas.tabHome.DatosUsuarioViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// --- constantes (evita duplicación) ---
private val EMAIL_REGEX = Regex("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$")
private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")

@Composable
fun LoginScreen(
    homeScreen: () -> Unit,
    navController: NavController,
    datosUsuarioViewModel: DatosUsuarioViewModel
) {
    // Estados UI
    var emailError by remember { mutableStateOf(false) }
    var emailErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var passwordError by remember { mutableStateOf(false) }
    var passwordErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    var attemptedLogin by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    // Context / ViewModel / focus
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    val focusManager = LocalFocusManager.current
    val resetPasswordUrl = "https://app.merkas.co/#/reset-password" // recomendable mover a strings.xml
    val forceShowError = attemptedLogin

    val mensaje by viewModel.message.collectAsState(initial = null)

    // Manejo de respuestas del ViewModel
    LaunchedEffect(mensaje) {
        when {
            mensaje?.contains("✅") == true -> {
                isLoading = false
                homeScreen()
            }
            mensaje?.contains("Email o contraseña incorrecto", ignoreCase = true) == true -> {
                isLoading = false
                loginErrorMessage = "Email o contraseña incorrecto"
            }
            mensaje != null -> {
                // maneja otros mensajes / errores
                isLoading = false
                loginErrorMessage = mensaje
            }
        }
    }

    // ROOT Box para poder superponer el Loading
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Detecta taps fuera para ocultar teclado sin interferir con clicks internos
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
            .padding(horizontal = 32.dp, vertical = 50.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            ) {
                BackButton(navController) // asegúrate que BackButton tiene contentDescription si es necesario
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(id = R.string.login),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(id = R.color.title)
                )
            }

            // FORM
            Column(modifier = Modifier.fillMaxSize()) {
                // EMAIL
                TextField(
                    label = stringResource(id = R.string.email),
                    value = correo,
                    onValueChange = {
                        correo = it
                        emailError = false
                        emailErrorType = ErrorType.NONE
                    },
                    isError = emailError,
                    errorType = emailErrorType,
                    onFocusLost = {
                        when {
                            correo.isBlank() -> {
                                emailError = true
                                emailErrorType = ErrorType.REQUIRED
                            }
                            !correo.matches(EMAIL_REGEX) -> {
                                emailError = true
                                emailErrorType = ErrorType.INVALID_FORMAT
                            }
                        }
                    },
                    placeholder = stringResource(id = R.string.emailExample) + "@correo.com",
                    forceShowError = forceShowError
                )

                // PASSWORD
                TextField(
                    label = stringResource(id = R.string.password),
                    imeAction = ImeAction.Done,
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        passwordError = false
                        passwordErrorType = ErrorType.NONE
                        loginErrorMessage = null
                    },
                    isError = passwordError,
                    errorType = passwordErrorType,
                    onFocusLost = {
                        when {
                            contrasena.isBlank() -> {
                                passwordError = true
                                passwordErrorType = ErrorType.REQUIRED
                            }
                            !contrasena.matches(PASSWORD_REGEX) -> {
                                passwordError = true
                                passwordErrorType = ErrorType.INVALID_FORMAT
                            }
                        }
                    },
                    placeholder = "",
                    isPassword = true,
                    forceShowError = forceShowError,
                    externalErrorMessage = loginErrorMessage
                )

                // FORGOT LINK
                TextButton(
                    onClick = {
                        val customTabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                        customTabsIntent.launchUrl(context, Uri.parse(resetPasswordUrl))
                    }
                ) {
                    Text(
                        stringResource(id = R.string.forgotPassword),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        color = colorResource(id = R.color.merkas),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BUTTON LOGIN
                ButtonAuth(
                    text = stringResource(R.string.login),
                    style = ButtonAuthStyle.Login,
                    // Deshabilitar si ya estamos cargando para evitar reenvíos
                    enabled = !isLoading,
                    onClick = {
                        // Previene doble click
                        if (isLoading) return@ButtonAuth

                        focusManager.clearFocus()
                        Log.d("Login", "Botón Iniciar presionado")
                        attemptedLogin = true

                        val validEmail = correo.matches(EMAIL_REGEX)
                        val passwordValid = contrasena.isNotBlank()

                        emailError = !validEmail
                        passwordError = !passwordValid
                        emailErrorType = if (!validEmail) ErrorType.INVALID_FORMAT else ErrorType.NONE
                        passwordErrorType = if (!passwordValid) ErrorType.REQUIRED else ErrorType.NONE

                        if (!validEmail || !passwordValid) return@ButtonAuth

                        loginErrorMessage = null
                        isLoading = true

                        viewModel.clearMessage()

                        // Ejecuta login; ViewModel debe emitir mensaje/estado y LaunchedEffect lo gestionará
                        viewModel.login(
                            correo = correo,
                            contrasena = contrasena,
                            datosUsuarioViewModel = datosUsuarioViewModel
                        )
                    }
                )
            }
        }

        // Overlay de carga: animado (opcional)
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Loading(message = "Iniciando sesión...")
        }
    }
}
