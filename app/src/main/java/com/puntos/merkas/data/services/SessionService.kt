package com.puntos.merkas.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

// ---------------------------
// BASE URL
// ---------------------------
private const val BASE_URL = "https://www.merkas.co/merkasbusiness/"

// ---------------------------
// DATA MODELS LOGIN
// ---------------------------
data class LoginData(
    val correo: String,
    val contrasena: String,
    val token: String
)

data class LoginErrorResponse(
    val mensaje: String
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

// Resultado login
sealed class LoginResult {
    data class Success(val data: LoginResponse) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

// ---------------------------
// DATA MODELS REGISTER
// ---------------------------
data class RegisterData(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val correo: String,
    val contrasena: String,
    val token: String
)

data class RegisterSuccessResponse(
    val validacion: String
)

data class RegisterErrorResponse(
    val mensaje: String
)

sealed class RegisterResult {
    data class Success(val data: RegisterSuccessResponse) : RegisterResult()
    data class Failure(val message: String) : RegisterResult()
}

// ---------------------------
// RETROFIT API
// ---------------------------
interface ApiSessionService {

    @POST("function-api.php?title=usuarios")
    suspend fun login(@Body data: LoginData): retrofit2.Response<LoginResponse>

    @Multipart
    @POST("function-api-registro.php")
    suspend fun register(
        @Query("title") title: String,
        @Part("tipo") tipo: String,
        @Part("usuario_id") usuarioId: String,
        @Part("fileimagen") fileImagen: String,
        @Part("usuario_social") usuarioSocial: String,
        @Part("usuario_social_imagen") usuarioSocialImagen: String,
        @Part("nombre") nombre: String,
        @Part("apellido") apellido: String,
        @Part("usuario_telefono") telefono: String,
        @Part("usuario_correo") correo: String,
        @Part("contrasena") contrasena: String,
        @Part("token") token: String,
        @Part("usuario_numero_documento") documento: String
    ): retrofit2.Response<RegisterSuccessResponse>
}

// ---------------------------
// SERVICE SINGLETON
// ---------------------------
object SessionService {

    private val api: ApiSessionService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiSessionService::class.java)
    }

    // LOGIN
    suspend fun login(data: LoginData): LoginResult {
        return withContext(Dispatchers.IO) {
            val response = api.login(data)

            val body = response.errorBody()?.string()
            if (body != null && body.contains("mensaje")) {
                return@withContext LoginResult.Failure(
                    Regex("\"mensaje\":\"(.*?)\"").find(body)?.groupValues?.get(1) ?: "Error"
                )
            }

            response.body()?.let {
                return@withContext LoginResult.Success(it)
            } ?: LoginResult.Failure("Error desconocido")
        }
    }

    // REGISTER
    suspend fun register(data: RegisterData, title: String): RegisterResult {
        return withContext(Dispatchers.IO) {

            val response = api.register(
                title = title,
                tipo = "normal",
                usuarioId = "",
                fileImagen = "",
                usuarioSocial = "",
                usuarioSocialImagen = "",
                nombre = data.nombre,
                apellido = data.apellido,
                telefono = data.telefono,
                correo = data.correo,
                contrasena = data.contrasena,
                token = data.token,
                documento = ""
            )

            val body = response.errorBody()?.string()
            if (body != null && body.contains("mensaje")) {
                return@withContext RegisterResult.Failure(
                    Regex("\"mensaje\":\"(.*?)\"").find(body)?.groupValues?.get(1) ?: "Error"
                )
            }

            response.body()?.let {
                return@withContext RegisterResult.Success(it)
            } ?: RegisterResult.Failure("Error desconocido")
        }
    }
}
