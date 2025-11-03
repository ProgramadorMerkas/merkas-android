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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

/*
object TokenService {

    // BASE URL
    private const val BASE_URL = "https://www.merkas.co/merkasbusiness/"

    private val client = NetworkClient.client


    /**
     * Devuelve el token generado si el backend lo acepta, sino null
     */
    suspend fun obtenerToken(): String? = withContext(Dispatchers.IO) {

        // Generar token UUID como en Swift
        val createToken = UUID.randomUUID().toString()
        Log.d("TOKEN_SERVICE", "Token generado localmente: $createToken")


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
        Log.d("TOKEN_SERVICE", "Respuesta del servidor: $responseBody")

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
 */

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
}

private val Context.dataStore by preferencesDataStore("session_store")

class TokenStore(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("user_token")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[TOKEN_KEY] }
            .firstOrNull()
    }

}