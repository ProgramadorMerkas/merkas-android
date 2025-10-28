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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.puntos.merkas.R
import com.puntos.merkas.components.buttons.BackButton
import com.puntos.merkas.components.buttons.ButtonAuth
import com.puntos.merkas.components.buttons.ButtonAuthStyle
import com.puntos.merkas.components.inputs.ErrorType
import com.puntos.merkas.components.inputs.TextField
import com.puntos.merkas.data.network.SignUpResult
import com.puntos.merkas.screens.auth.PasswordRequirements
import com.puntos.merkas.screens.auth.validatePassword

@Composable
fun SignUpScreen (
    homeScreen: () -> Unit,
    navController: NavController
) {
    // Estado de los campos
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var lastName by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf(false) }

    // var phonePrefix by remember { mutableStateOf("+57") }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var passwordErrorType by remember { mutableStateOf(ErrorType.NONE) }

    var attemptedSignUp by remember { mutableStateOf(false) }

    // Foco del TextField
    val focusManager = LocalFocusManager.current

    val forceShowError = attemptedSignUp

    val signUpViewModel: SignUpViewModel = viewModel()
    val signUpState by signUpViewModel.signUpState.collectAsState()
    val isLoading by signUpViewModel.isLoading.collectAsState()

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
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                isError = nameError,
                errorType = null,
                onFocusLost = { if (name.isBlank()) nameError = true },
                placeholder = "",
                forceShowError = forceShowError
            )

            // APELLIDO
            TextField(
                label = stringResource(id = R.string.lastname),
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = false
                },
                isError = lastNameError,
                errorType = null,
                onFocusLost = { if (lastName.isBlank()) lastNameError = true },
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
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = false
                    },
                    isError = phoneError,
                    errorType = null,
                    onFocusLost = { if (phoneNumber.isBlank()) phoneError = true },
                    placeholder = "",
                    forceShowError = forceShowError
                )
            }

            // EMAIL
            TextField(
                label = stringResource(id = R.string.email),
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                    emailErrorType = ErrorType.NONE
                },
                isError = emailError,
                errorType = emailErrorType,
                onFocusLost = {
                    when {
                        email.isBlank() -> {
                            emailError = true
                            emailErrorType = ErrorType.REQUIRED
                        }
                        !email.matches(Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) -> {
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
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    passwordErrorType = ErrorType.NONE
                },
                isError = passwordError,
                errorType = passwordErrorType,
                onFocusLost = {
                    if (password.isBlank()) {
                        passwordError = true
                        passwordErrorType = ErrorType.REQUIRED
                    }
                },
                placeholder = "",
                isPassword = true,
                imeAction = ImeAction.Done,
                forceShowError = forceShowError
            )

            PasswordRequirements(password)

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
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
                onClick = {
                    Log.d("SignUpScreen", "Botón Registrarse presionado")
                    attemptedSignUp = true
                    if (email.isBlank()) {
                        emailError = true
                        emailErrorType = ErrorType.REQUIRED
                    } else if (!email.matches(Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"))) {
                        emailError = true
                        emailErrorType = ErrorType.INVALID_FORMAT
                    }

                    val passwordState = validatePassword(password)
                    val passwordIsValid = with(passwordState) {
                        hasMinLength && hasUppercase && hasLowerCase && hasNumber && hasSpecialChar
                    }

                    if (password.isBlank()) {
                        passwordError = true
                        passwordErrorType = ErrorType.REQUIRED
                    } else if (!passwordIsValid) {
                        passwordError = true
                        passwordErrorType = ErrorType.INVALID_FORMAT
                    }

                    if (!emailError && !passwordError && !nameError && !lastNameError && !phoneError) {
                        Log.d("SignUpScreen", "Datos válidos, ejecutando signUpViewModel.signUp()")

                        signUpViewModel.signUp(
                            nombre = name,
                            apellido = lastName,
                            telefono = phoneNumber,
                            correo = email,
                            contrasena = password,
                            token = "",
                            title = "usuarios"
                        )
                    } else {
                        Log.d("SignUpScreen", "Errores detectados")
                    }
                }
            )
            when (val result = signUpState) {
                is SignUpResult.Success -> {
                    Log.d("SignUpScreen", "Registro exitoso, navegando a home")
                    homeScreen()
                    signUpViewModel.resetState()
                }
                is SignUpResult.Failure -> {
                    Text("Error: ${result.message}",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp))
                }

                null -> Unit
            }
        }
    }
}