package com.puntos.merkas.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.network.ApiClient
import com.puntos.merkas.data.network.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.service.login(
                    body = mapOf("email" to email, "password" to password),
                    token = "Bearer $token"
                )
                _loginState.value = LoginResult.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginResult.Failure("Error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _loginState.value = null
    }
}

/*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.models.LoginData
import com.puntos.merkas.data.network.LoginResult
import com.puntos.merkas.data.network.LoginService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val service = LoginService.instance

    fun login(correo: String, contrasena: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = null // reset antes de iniciar

            val data = LoginData(
                correo = correo,
                contrasena = contrasena,
                token = token
            )

            val result = service.login(data)
            println("🔥 RESULTADO LOGIN: $result")
            _loginState.value = result

            _isLoading.value = false
        }
    }

    fun resetState() {
        _loginState.value = null
        _isLoading.value = false
    }
}


 */

