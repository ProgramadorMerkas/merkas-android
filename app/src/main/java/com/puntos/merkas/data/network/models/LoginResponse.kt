package com.puntos.merkas.data.network.models

data class LoginResponse(
    val token: String,
    val userId: String,
    val email: String
)