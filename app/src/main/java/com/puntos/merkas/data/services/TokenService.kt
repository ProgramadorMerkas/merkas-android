package com.puntos.merkas.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

object TokenService {

    // BASE URL
    private const val BASE_URL = "https://www.merkas.co/merkasbusiness/"

    private val client = OkHttpClient()

    /**
     * Devuelve el token generado si el backend lo acepta, sino null
     */
    suspend fun obtenerToken(): String? = withContext(Dispatchers.IO) {

        // Generar token UUID como en Swift
        val createToken = UUID.randomUUID().toString()

        // Cuerpo JSON como Dictionary en Swift
        val jsonBody = JSONObject().apply {
            put("create_token", createToken)
            put("token", "7242b219185a6ecd76e2f0de1a178928")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${BASE_URL}function-api-token.php?title=token")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string() ?: return@withContext null
        val json = JSONObject(responseBody)

        val mensaje = json.optString("mensaje", "")

        return@withContext if (mensaje.isNotEmpty()) {
            // Backend acepta → retornamos el createToken, como en Swift
            createToken
        } else {
            null
        }
    }
}
