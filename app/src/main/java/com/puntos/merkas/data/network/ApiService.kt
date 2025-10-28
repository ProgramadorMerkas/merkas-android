package com.puntos.merkas.data.network

import com.puntos.merkas.data.network.models.LoginResponse
import com.puntos.merkas.data.network.models.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): LoginResponse

    @POST("auth/signup")
    suspend fun signUp(
        @Body body: Map<String, String>
    ): SignUpResponse
}