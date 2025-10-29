package com.puntos.merkas.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.services.LoginData
import com.puntos.merkas.data.services.LoginResult
import com.puntos.merkas.data.services.LoginService
import com.puntos.merkas.data.services.RegisterData
import com.puntos.merkas.data.services.RegisterResult
import com.puntos.merkas.data.services.RegisterService
import com.puntos.merkas.services.TokenService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

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
            val token = TokenService.obtenerToken()

            if (token == null) {
                _message.value = "Error obteniendo token"
                _loading.value = false
                return@launch
            }
            Log.d("LOGIN_TOKEN", token)

            // 2. Enviar login al backend
            val result = LoginService.login(LoginData(correo = correo, contrasena = contrasena, token = token))

            when(result) {
                is LoginResult.Success -> {
                    val user = result.data
                    println("✅ Login correcto: ${user.usuario_nombre_completo}")
                }
                is LoginResult.Failure -> {
                    println("❌ Error de login: ${result.message}")
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

            val token = TokenService.obtenerToken()
            if (token == null) {
                _message.value = "Error obteniendo token"
                _loading.value = false
                return@launch
            }
            Log.d("SIGNUP_TOKEN", token)


            val result = RegisterService.register(data = RegisterData(nombre = nombre, apellido = apellido,
                telefono = telefono, correo = correo, contrasena = contrasena, token = token), title = "usuarios")

            when (result) {
                is RegisterResult.Success -> {
                    println("✅ Registro exitoso: ${result.data.validacion}")
                }
                is RegisterResult.Failure -> {
                    println("❌ Error de registro: ${result.message}")
                }
            }

            _loading.value = false
        }
    }
}
