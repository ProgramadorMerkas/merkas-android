package com.puntos.merkas.data.network

import com.puntos.merkas.data.network.models.LoginResponse

sealed class LoginResult {
    data class Success(val data: LoginResponse): LoginResult()
    data class Failure(val message: String): LoginResult()
}