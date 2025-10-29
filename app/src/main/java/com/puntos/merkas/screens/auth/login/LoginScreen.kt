package com.puntos.merkas.screens.auth.login

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.puntos.merkas.components.buttons.BackButton
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle
import com.puntos.merkas.components.inputs.ErrorType
import com.puntos.merkas.components.inputs.TextField
import com.puntos.merkas.data.services.LoginResult
import com.puntos.merkas.services.TokenService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen (
    homeScreen: () -> Unit,
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // Estado del texto
    var emailError by remember { mutableStateOf(false) }
    var emailErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var passwordError by remember { mutableStateOf(false) }
    var passwordErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    var attemptedLogin by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Foco del TextField
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val resetPasswordUrl = "https://app.merkas.co/#/reset-password"
    val forceShowError = attemptedLogin

    val message by viewModel.message.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 50.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        ) {
            // BOTÓN ATRÁS
            BackButton(navController)

            Spacer(modifier = Modifier.height(20.dp))

            // TEXTO INICIAR SESIÓN
            Text(
                stringResource(id = R.string.login),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.title)
            )
        }

            Column(
                Modifier.fillMaxSize()
            ) {
                // CAMPO EMAIL
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
                            !correo.matches(Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) -> {
                                emailError = true
                                emailErrorType = ErrorType.INVALID_FORMAT
                            }
                        }
                    },
                    placeholder = stringResource(id = R.string.emailExample) + "@correo.com",
                    forceShowError = forceShowError // MOSTRAR ERROR SI NO SE HA EJECUTADO HASBEENFOCUSED ANTERIORMENTE
                )

                // CAMPO CONTRASEÑA
                TextField(
                    label = stringResource(id = R.string.password),
                    imeAction = ImeAction.Done,
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        passwordError = false
                        passwordErrorType = ErrorType.NONE
                    },
                    isError = passwordError,
                    errorType = passwordErrorType,
                    onFocusLost = {
                        when {
                            contrasena.isBlank() -> {
                                passwordError = true
                                passwordErrorType = ErrorType.REQUIRED
                            }
                            !contrasena.matches(
                                Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
                            ) -> {
                                passwordError = true
                                passwordErrorType = ErrorType.INVALID_FORMAT
                            }
                        }
                    },
                    placeholder = "",
                    isPassword = true,
                    forceShowError = forceShowError // MOSTRAR ERROR SI NO SE HA EJECUTADO HASBEENFOCUSED ANTERIORMENTE
                )

                // LINK OLVIDÉ MI CONTRASEÑA
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

                // BOTÓN LOGIN
                ButtonAuth(
                    text = stringResource(R.string.login),
                    style = ButtonAuthStyle.Login,
                    onClick = {
                        Log.d("Login", "Botón Iniciar presionado")
                        // Validación manual antes de enviar
                        attemptedLogin = true

                        val validEmail = correo.matches(Regex(
                            "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"))

                        if (!validEmail) {
                            emailError = true
                            emailErrorType = ErrorType.INVALID_FORMAT
                        }


                        val passwordValid = contrasena.isNotBlank()

                        // Si hay errores, los marcamos
                        emailError = !validEmail
                        passwordError = !passwordValid
                        emailErrorType = if (!validEmail) ErrorType.INVALID_FORMAT else ErrorType.NONE
                        passwordErrorType = if (!passwordValid) ErrorType.REQUIRED else ErrorType.NONE

                        // Si algo es inválido, salimos
                        if (!validEmail || !passwordValid) return@ButtonAuth

                        // ✅ AQUÍ sí llamamos al ViewModel **solamente una vez**
                        viewModel.login(correo, contrasena)

                        /* if (validEmail) {
                             Log.d("Login", "Datos válidos, ejecutando loginViewModel.login()")
                             viewModel.login(email, password)
                         } else {
                             Log.d("Login", "Errores detectados")
                         }

                          var valid = true

                         if (email.isBlank()) {
                             emailError = true
                             emailErrorType = ErrorType.REQUIRED
                             valid = false
                         } else if (!email.matches(
                                 Regex(
                                     "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
                                 )
                             )
                         ) {
                             emailError = true
                             emailErrorType = ErrorType.INVALID_FORMAT
                             valid = false
                         }

                         if (password.isBlank()) {
                             passwordError = true
                             passwordErrorType = ErrorType.REQUIRED
                             valid = false
                         } else if (!password.matches(
                                 Regex(
                                     "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$"
                                 )
                             )
                         ) {
                             passwordError = true
                             passwordErrorType = ErrorType.INVALID_FORMAT
                             valid = false
                         }

                         if (valid) {
                             viewModel.login(email, password)
                         }*/
                    }
                )

                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = colorResource(R.color.merkas))
                }
                    /* when (loginState) {
                        is LoginResult.Success -> {
                            LaunchedEffect(Unit) {
                                homeScreen()
                                viewModel.resetState()
                            }
                        }
                        is LoginResult.Failure -> {
                            val message = (loginState as LoginResult.Failure).message
                            Text(
                                message,
                                color = colorResource(R.color.merkas),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        null -> {}
                    } */
            }
    }
}