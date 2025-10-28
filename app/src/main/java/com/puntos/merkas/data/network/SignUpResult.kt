package com.puntos.merkas.data.network

import com.puntos.merkas.data.network.models.SignUpResponse

sealed class SignUpResult {
    data class Success(val data: SignUpResponse): SignUpResult()
    data class Failure(val message: String): SignUpResult()
}