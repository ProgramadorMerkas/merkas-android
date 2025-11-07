package com.puntos.merkas.data.services

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.puntos.merkas.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

object TokenService {
    private val client = NetworkClient.client

    suspend fun obtenerToken(tokenStore: TokenStore): String? = withContext(Dispatchers.IO) {

        val createToken = UUID.randomUUID().toString()
        Log.d("TOKEN_SERVICE", "Token generado localmente: $createToken")

        val jsonBody = JSONObject().apply {
            put("create_token", createToken)
            put("token", "7242b219185a6ecd76e2f0de1a178928")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${baseURL}/function-api-token.php?title=token")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            Log.e("TOKEN_SERVICE", "Error en request: ${e.message}")
            return@withContext null
        }

        val responseBody = response.body?.string() ?: run {
            Log.e("TOKEN_SERVICE", "Respuesta vacía del servidor")
            return@withContext null
        }
        Log.d("TOKEN_SERVICE", "Respuesta del servidor: $responseBody")

        val json = JSONObject(responseBody)
        val mensaje = json.optString("mensaje", "")

        return@withContext if (mensaje.isNotEmpty()) {
            // Backend acepta → guardamos token en DataStore y lo retornamos
            try {
                tokenStore.saveToken(createToken)
                Log.d("TOKEN_SERVICE", "Token guardado en TokenStore: $createToken")
            } catch (e: Exception) {
                Log.e("TOKEN_SERVICE", "No se pudo guardar token: ${e.message}")
            }
            createToken
        } else {
            Log.e("TOKEN_SERVICE", "Backend no devolvió 'mensaje' → token no aceptado")
            null
        }
    }

    suspend fun saveUserSessionToken(tokenStore: TokenStore, token: String) {
        tokenStore.saveToken(token)
    }

}

private val Context.dataStore by preferencesDataStore("session_store")

class TokenStore(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("user_token")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // 🟢 Guardar token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // 🟢 Guardar datos de usuario (nombre y correo)
    suspend fun saveUserData(nombre: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = nombre
            prefs[USER_EMAIL_KEY] = email
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    // 🟢 Obtener token
    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[TOKEN_KEY] }
            .firstOrNull()
    }

    // 🟢 Obtener datos del usuario
    suspend fun getUserData(): Pair<String?, String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_NAME_KEY] to prefs[USER_EMAIL_KEY]
        }.first()
    }

    // 🟢 Borrar to-do (logout)
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
