package com.puntos.merkas.data.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

// =============================
// ==== LOGIN DATA STRUCTS ====
// =============================

data class LoginData(
    val correo: String,
    val contrasena: String,
    val token: String
)

data class LoginResponse(
    val usuario_id: String,
    val usuario_codigo: String,
    val usuario_nombre: String,
    val usuario_apellido: String,
    val usuario_nombre_completo: String,
    val usuario_correo: String,
    val usuario_telefono: String,
    val usuario_whatssap: String,
    val usuario_numero_documento: String,
    val usuario_tipo_documento: String,
    val usuario_genero: String,
    val usuario_direccion: String,
    val usuario_rol_principal: String,
    val usuario_status: String,
    val usuario_estado: String,
    val usuario_fecha_registro: String,
    val usuario_merkash: String,
    val usuario_puntos: String,
    val usuario_id_padre: String?,
    val municipio_id: String?,
    val usuario_ruta_img: String,
    val imagen: String,
    val usuario_latitud: String?,
    val usuario_longitud: String?,
    val usuario_bienvenida: String?,
    val usuario_contrasena: String?,
    val usuario_token_contrasena: String?,
    val usuario_token_fecha: String?,
    val usuario_token_merkash: String?,
    val usuario_token_merkash_fecha: String?,
    val usuario_terminos: String?,
    val usuario_last_login: String?
)

data class LoginErrorResponse(
    val mensaje: String
)

sealed class LoginResult {
    data class Success(val data: LoginResponse) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

// ==============================
// ==== REGISTER DATA STRUCTS ===
// ==============================

data class RegisterData(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val correo: String,
    val contrasena: String,
    val tokenStore: TokenStore
)

data class RegisterSuccessResponse(val validacion: String)

data class RegisterErrorResponse(val mensaje: String)

sealed class RegisterResult {
    data class Success(val data: RegisterSuccessResponse): RegisterResult()
    data class Failure(val message: String): RegisterResult()
}

// =============================
// ======== LOGIN SERVICE ======
// =============================

object LoginService {

    // Cliente OkHttp con logs visibles en Logcat
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    suspend fun login(data: LoginData): LoginResult = withContext(Dispatchers.IO) {
        val url = URL("${baseURL}/function-api.php?title=usuarios")

        try {
            val jsonBody = JSONObject().apply {
                put("correo", data.correo)
                put("contrasena", data.contrasena)
                put("token", data.token)
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: return@withContext LoginResult.Failure("Respuesta vacía")

            Log.e("LOGIN_URL", url.toString())
            Log.e("LOGIN_RESPONSE", responseString)

            if (!response.isSuccessful) {
                return@withContext LoginResult.Failure("HTTP Error ${response.code}")
            }

            val json = JSONObject(responseString)

            return@withContext if (json.has("mensaje")) {
                LoginResult.Failure(json.getString("mensaje"))
            } else {
                LoginResult.Success(parseLoginResponse(json))
            }

        } catch (e: Exception) {
            return@withContext LoginResult.Failure("Error de red: ${e.localizedMessage}")
        }
    }

    private fun parseLoginResponse(obj: JSONObject): LoginResponse {
        return LoginResponse(
            usuario_id = obj.optString("usuario_id"),
            usuario_codigo = obj.optString("usuario_codigo"),
            usuario_nombre = obj.optString("usuario_nombre"),
            usuario_apellido = obj.optString("usuario_apellido"),
            usuario_nombre_completo = obj.optString("usuario_nombre_completo"),
            usuario_correo = obj.optString("usuario_correo"),
            usuario_telefono = obj.optString("usuario_telefono"),
            usuario_whatssap = obj.optString("usuario_whatssap"),
            usuario_numero_documento = obj.optString("usuario_numero_documento"),
            usuario_tipo_documento = obj.optString("usuario_tipo_documento"),
            usuario_genero = obj.optString("usuario_genero"),
            usuario_direccion = obj.optString("usuario_direccion"),
            usuario_rol_principal = obj.optString("usuario_rol_principal"),
            usuario_status = obj.optString("usuario_status"),
            usuario_estado = obj.optString("usuario_estado"),
            usuario_fecha_registro = obj.optString("usuario_fecha_registro"),
            usuario_merkash = obj.optString("usuario_merkash"),
            usuario_puntos = obj.optString("usuario_puntos"),
            usuario_id_padre = obj.optString("usuario_id_padre", null),
            municipio_id = obj.optString("municipio_id", null),
            usuario_ruta_img = obj.optString("usuario_ruta_img"),
            imagen = obj.optString("imagen"),
            usuario_latitud = obj.optString("usuario_latitud", null),
            usuario_longitud = obj.optString("usuario_longitud", null),
            usuario_bienvenida = obj.optString("usuario_bienvenida", null),
            usuario_contrasena = obj.optString("usuario_contrasena", null),
            usuario_token_contrasena = obj.optString("usuario_token_contrasena", null),
            usuario_token_fecha = obj.optString("usuario_token_fecha", null),
            usuario_token_merkash = obj.optString("usuario_token_merkash", null),
            usuario_token_merkash_fecha = obj.optString("usuario_token_merkash_fecha", null),
            usuario_terminos = obj.optString("usuario_terminos", null),
            usuario_last_login = obj.optString("usuario_last_login", null)
        )
    }
}

// =============================
// ===== REGISTER SERVICE ======
// =============================


object RegisterService {

    suspend fun register(data: RegisterData, title: String): RegisterResult = withContext(Dispatchers.IO) {
        val url = URL("${baseURL}/function-api.php?title=$title")
        val boundary = "Boundary-${System.currentTimeMillis()}"
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Authorization", "Bearer ${data.tokenStore}")
            doOutput = true
        }

        try {
            val body = buildString {
                appendFormField("tipo", "normal", boundary)
                appendFormField("usuario_id", "", boundary)
                appendFormField("fileimagen", "", boundary)
                appendFormField("usuario_social", "", boundary)
                appendFormField("usuario_social_imagen", "", boundary)
                appendFormField("nombre", data.nombre, boundary)
                appendFormField("apellido", data.apellido, boundary)
                appendFormField("usuario_telefono", data.telefono, boundary)
                appendFormField("usuario_correo", data.correo, boundary)
                appendFormField("contrasena", data.contrasena, boundary)
                appendFormField("usuario_numero_documento", "", boundary)
                appendFormField("token", data.tokenStore.toString(), boundary)
                append("--$boundary--\r\n")
            }

            Log.d("REGISTER_DEBUG", "Body completo:\n$body")

            connection.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }

            Log.e("REGISTER_URL", url.toString())
            Log.e("REGISTER_RESPONSE", response)

            if (code !in 200..299)
                return@withContext RegisterResult.Failure("HTTP Error $code")

            if (!response.trim().startsWith("{"))
                return@withContext RegisterResult.Failure("Respuesta no JSON: $response")

            val json = JSONObject(response)

            return@withContext if (json.has("mensaje")) {
                RegisterResult.Failure(json.getString("mensaje"))
            } else {
                RegisterResult.Success(RegisterSuccessResponse(json.getString("validacion")))
            }

        } catch (e: Exception) {
            RegisterResult.Failure(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
        }
    }

    private fun StringBuilder.appendFormField(name: String, value: String, boundary: String) {
        append("--$boundary\r\n")
        append("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        append("$value\r\n")
    }
}

object SessionService {
    suspend fun cerrarSesion(tokenStore: TokenStore) {
        tokenStore.clearToken()
    }
}
