package com.puntos.merkas.screens.merkas.tabHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntos.merkas.data.services.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DatosUsuarioViewModel : ViewModel() {

    private val _datosUsuario = MutableStateFlow<LoginResponse?>(null)
    val datosUsuario: StateFlow<LoginResponse?> = _datosUsuario

    fun setDatosUsuario(usuarioData: LoginResponse) {
        viewModelScope.launch {
            _datosUsuario.value = usuarioData
        }
    }

    fun clearDatosUsuario() {
        _datosUsuario.value = null
    }
}