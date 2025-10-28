package com.puntos.merkas.data.services

import com.puntos.merkas.data.services.Environment.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

object TokenService {
    // Similar a TokenService.obtenerToken(baseURL:) del Swift
    // Devuelve createToken (UUID) si backend acepta, o null si no.
    suspend fun obtenerToken(): String? = withContext(Dispatchers.IO) {
        try {
            val createToken = UUID.randomUUID().toString()
            val bodyJson = JSONObject().apply {
                put("create_token", createToken)
                put("token", "7242b219185a6ecd76e2f0de1a178928")
            }
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = bodyJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${BASE_URL}function-api-token.php?title=token")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string()
                if (!response.isSuccessful || respBody == null) {
                    return@withContext null
                }
                val json = JSONObject(respBody)
                val mensaje = json.optString("mensaje", "")
                if (mensaje.isNotEmpty()) {
                    // backend aceptó → guardamos createToken equivalente
                    return@withContext createToken
                }
                return@withContext null
            }
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
