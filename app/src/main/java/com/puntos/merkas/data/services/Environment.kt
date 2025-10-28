package com.puntos.merkas.data.services

// Equivalente a environment.swift

object Environment {

    enum class EnvironmentType {
        PRODUCTION,
        STAGING,
        LOCAL
    }

    // Cambia aquí el entorno actual si lo necesitas
    private val currentEnvironment = EnvironmentType.PRODUCTION

    val BASE_URL: String
        get() = when (currentEnvironment) {
            EnvironmentType.PRODUCTION -> "https://api.merkas.co/"
            EnvironmentType.STAGING -> "https://staging.merkas.co/"
            EnvironmentType.LOCAL -> "http://10.0.2.2:8000/" // localhost para emulador Android
        }
}

