package com.puntos.merkas.screens.auth.signup

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.puntosmerkas.co.R
import com.puntos.merkas.auth.AuthViewModel
import com.puntos.merkas.auth.AuthViewModelFactory
import com.puntos.merkas.components.buttons.BackButton
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle
import com.puntos.merkas.components.inputs.ErrorType
import com.puntos.merkas.components.inputs.TextField
import com.puntos.merkas.screens.auth.PasswordRequirements
import com.puntos.merkas.screens.auth.validatePassword

@Composable
fun SignUpScreen (
    homeScreen: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Estado de los campos
    var nameError by remember { mutableStateOf(false) }

    var lastNameError by remember { mutableStateOf(false) }

    // var phonePrefix by remember { mutableStateOf("+57") }
    var phoneError by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var emailErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var passwordError by remember { mutableStateOf(false) }
    var passwordErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var attemptedSignUp by remember { mutableStateOf(false) }

    // Foco del TextField
    val focusManager = LocalFocusManager.current

    val forceShowError = attemptedSignUp

    val message by viewModel.message.collectAsState(initial = null)
    val loading by viewModel.loading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 50.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        ) {
            BackButton(navController)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                stringResource(id = R.string.signup),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.title)
            )
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
        )
        {
            // NOMBRE
            TextField(
                label = stringResource(id = R.string.name),
                value = nombre,
                onValueChange = {
                    nombre = it
                    nameError = false
                },
                isError = nameError,
                errorType = null,
                onFocusLost = { if (nombre.isBlank()) nameError = true },
                placeholder = "",
                forceShowError = forceShowError
            )

            // APELLIDO
            TextField(
                label = stringResource(id = R.string.lastname),
                value = apellido,
                onValueChange = {
                    apellido = it
                    lastNameError = false
                },
                isError = lastNameError,
                errorType = null,
                onFocusLost = { if (apellido.isBlank()) lastNameError = true },
                placeholder = "",
                forceShowError = forceShowError
            )

            // TELÉFONO (PREFIJO+NÚMERO)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = "+57",
                    onValueChange = {},       // no hace nada
                    enabled = false,          // deshabilita entrada
                    readOnly = true,          // evita selección / foco
                    singleLine = true,
                    label = { Text("") },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .width(70.dp)
                        .padding(top = 20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.Transparent,
                    )
                )
                // NÚMERO DE TELÉFONO
                TextField(
                    label = stringResource(id = R.string.phoneNumber),
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        phoneError = false
                    },
                    isError = phoneError,
                    errorType = null,
                    onFocusLost = { if (telefono.isBlank()) phoneError = true },
                    placeholder = "",
                    forceShowError = forceShowError
                )
            }

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
                        !correo.matches(Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) -> {
                            emailError = true
                            emailErrorType = ErrorType.INVALID_FORMAT
                        }
                    }
                },
                placeholder = stringResource(id = R.string.emailExample) + "@correo.com",
                forceShowError = forceShowError
            )

            // CONTRASEÑA
            TextField(
                label = stringResource(id = R.string.password),
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    passwordError = false
                    passwordErrorType = ErrorType.NONE
                },
                isError = passwordError,
                errorType = passwordErrorType,
                onFocusLost = {
                    if (contrasena.isBlank()) {
                        passwordError = true
                        passwordErrorType = ErrorType.REQUIRED
                    }
                },
                placeholder = "",
                isPassword = true,
                imeAction = ImeAction.Done,
                forceShowError = forceShowError
            )

            PasswordRequirements(contrasena)

            Spacer(modifier = Modifier.height(40.dp))


            if (loading) {
                Text(
                    "Registrando...",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // BOTÓN SIGNUP
            ButtonAuth(
                text = stringResource(R.string.signup),
                style = ButtonAuthStyle.Login,
                enabled = !loading,
                onClick = {
                    Log.d("SignUpScreen", "Botón Registrarse presionado")
                    attemptedSignUp = true

                    // ---- VALIDAR EMAIL ----
                    if (correo.isBlank()) {
                        emailError = true
                        emailErrorType = ErrorType.REQUIRED
                    } else if (!correo.matches(Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"))) {
                        emailError = true
                        emailErrorType = ErrorType.INVALID_FORMAT
                    } else {
                        emailError = false
                    }

                    // ---- VALIDAR CONTRASEÑA ----
                    val passwordState = validatePassword(contrasena)
                    val passwordIsValid = with(passwordState) {
                        hasMinLength && hasUppercase && hasLowerCase && hasNumber && hasSpecialChar
                    }

                    if (contrasena.isBlank()) {
                        passwordError = true
                        passwordErrorType = ErrorType.REQUIRED
                    } else if (!passwordIsValid) {
                        passwordError = true
                        passwordErrorType = ErrorType.INVALID_FORMAT
                    } else {
                        passwordError = false
                    }

                    // ---- VALIDAR OTROS DATOS ----
                    nameError = nombre.isBlank()
                    lastNameError = apellido.isBlank()
                    phoneError = telefono.isBlank()

                    // ---- SI TO/DO CORRECTO -> REGISTRAR ----
                    if (!emailError && !passwordError && !nameError && !lastNameError && !phoneError) {
                        Log.d("SignUpScreen", "Datos válidos, ejecutando signUpViewModel.signUp()")
                        viewModel.register(nombre, apellido, telefono, correo, contrasena)

                    } else {
                        Log.d("SignUpScreen", "Errores detectados")
                    }
                }
            )
        }
    }
}