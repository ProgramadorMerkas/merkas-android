package com.puntos.merkas.screens.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.network.ApiClient
import com.puntos.merkas.data.network.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpResult?>(null)
    val signUpState: StateFlow<SignUpResult?> = _signUpState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun signUp(
        nombre: String,
        apellido: String,
        telefono: String,
        correo: String,
        contrasena: String,
        token: String,
        title: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.service.signUp(
                    mapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "telefono" to telefono,
                        "correo" to correo,
                        "contrasena" to contrasena,
                        "title" to title
                    )
                )
                _signUpState.value = SignUpResult.Success(response)
            } catch (e: Exception) {
                _signUpState.value = SignUpResult.Failure("Error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _signUpState.value = null
    }
}

/*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.models.SignUpData
import com.puntos.merkas.data.network.SignUpResult
import com.puntos.merkas.data.network.SignUpService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpResult?>(null)
    val signUpState: StateFlow<SignUpResult?> = _signUpState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val service = SignUpService.instance

    fun signUp(
        nombre: String,
        apellido: String,
        telefono: String,
        correo: String,
        contrasena: String,
        token: String,
        title: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _signUpState.value = null

            val data = SignUpData(
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                correo = correo,
                contrasena = contrasena,
                token = token
            )

            val result = service.signUp(data, title)
            _signUpState.value = result
            _isLoading.value = false
        }
    }

    fun resetState() {
        _signUpState.value = null
        _isLoading.value = false
    }
}


 */