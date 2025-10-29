package com.puntos.merkas.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.services.LoginData
import com.puntos.merkas.data.services.LoginResult
import com.puntos.merkas.data.services.RegisterData
import com.puntos.merkas.data.services.RegisterResult
import com.puntos.merkas.data.services.SessionService
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
            when (val result = SessionService.login(LoginData(correo, contrasena, token))) {
                is LoginResult.Success -> {
                    _message.value = "Bienvenido ${result.data.usuario_nombre}"
                }
                is LoginResult.Failure -> {
                    _message.value = result.message
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


            val data = RegisterData(nombre, apellido, telefono, correo, contrasena, token)

            when (val result = SessionService.register(data, title = "usuarios")) {
                is RegisterResult.Success -> {
                    _message.value = "Registro exitoso ✅"
                }
                is RegisterResult.Failure -> {
                    _message.value = result.message
                }
            }

            _loading.value = false
        }
    }
}
