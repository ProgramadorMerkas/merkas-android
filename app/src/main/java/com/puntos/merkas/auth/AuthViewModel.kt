package com.puntos.merkas.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.services.LoginData
import com.puntos.merkas.data.services.LoginResult
import com.puntos.merkas.data.services.LoginService
import com.puntos.merkas.data.services.RegisterData
import com.puntos.merkas.data.services.RegisterResult
import com.puntos.merkas.data.services.RegisterService
import com.puntos.merkas.data.services.TokenService
import com.puntos.merkas.data.services.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val tokenStore: TokenStore) : ViewModel() {

    // Estado para la UI (Compose observará este flow)
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /**
     * LOGIN
     */
    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            _loading.value = true

            // 1. Obtener token dinámico
            val token = TokenService.obtenerToken(tokenStore)

            if (token == null) {
                _message.value = "Error obteniendo token"
                _loading.value = false
                return@launch
            }
            Log.d("LOGIN_TOKEN", "Token generado: $token")

            // 2. Enviar login al backend
            val result = LoginService.login(
                LoginData(
                    correo = correo,
                    contrasena = contrasena,
                    token = token
                )
            )

            Log.d("LOGIN_RESULT_DEBUG", result.toString())


            when(result) {
                is LoginResult.Success -> {
                    val user = result.data
                    println("✅ Login correcto: ${user.usuario_nombre_completo}")

                    // tokenStore.saveToken(token)
                    Log.d("TOKEN_STORE", "Token guardado en DataStore: $token")

                    _message.value = "✅ Bienvenido ${result.data.usuario_nombre_completo}"
                    Log.d("LOGIN","✅ Usuario loggeado: ${result.data.usuario_nombre_completo}")
                }
                is LoginResult.Failure -> {
                    _message.value = "❌ ${result.message}"
                    Log.e("LOGIN_ERROR", result.message)
                }
            }
            _loading.value = false
        }
    }

    /**
     * REGISTRO
     */
    fun register(nombre: String, apellido: String, telefono: String, correo: String, contrasena: String) {
        viewModelScope.launch {

            _loading.value = true

            suspend fun attemptRegister(): RegisterResult {
                val token = TokenService.obtenerToken(tokenStore)
                if (token == null) {
                    return RegisterResult.Failure("Error obteniendo token")
                }

                return RegisterService.register(
                    data = RegisterData(
                        nombre = nombre,
                        apellido = apellido,
                        telefono = telefono,
                        correo = correo,
                        contrasena = contrasena,
                        tokenStore = tokenStore
                    ),
                    title = "registro",
                )
            }

            // Primer intento
            var result = attemptRegister()

            // Si el token fue incorrecto → intentamos 1 sola vez más
            if (result is RegisterResult.Failure && result.message == "token_incorrecto") {
                Log.d("SIGNUP", "♻️ Token incorrecto. Reintentando...")
                result = attemptRegister()
            }

            // Result final
            when (result) {
                is RegisterResult.Success -> {
                    println("✅ Registro exitoso: ${result.data.validacion}")
                    _message.value = "Registro exitoso ✅"
                }
                is RegisterResult.Failure -> {
                    println("❌ Error de registro: ${result.message}")
                    _message.value = "Error: ${result.message}"
                }
            }
            _loading.value = false
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val tokenStore = TokenStore(context)
        return AuthViewModel(tokenStore) as T
    }
}
