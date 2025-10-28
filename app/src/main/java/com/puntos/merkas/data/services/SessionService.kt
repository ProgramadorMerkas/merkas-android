package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import com.puntos.merkas.data.services.Environment.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Multipart

// Login/Register data models

data class LoginData(
    val correo: String,
    val contrasena: String,
    val token: String
)

data class LoginErrorResponse(val mensaje: String)

sealed class LoginResult {
    data class Success(val response: LoginResponse) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

data class LoginResponse(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("usuario_codigo") val usuarioCodigo: String,
    @SerializedName("usuario_nombre") val usuarioNombre: String,
    @SerializedName("usuario_apellido") val usuarioApellido: String,
    @SerializedName("usuario_nombre_completo") val usuarioNombreCompleto: String,
    @SerializedName("usuario_correo") val usuarioCorreo: String,
    @SerializedName("usuario_telefono") val usuarioTelefono: String,
    @SerializedName("usuario_whatssap") val usuarioWhatssap: String,
    @SerializedName("usuario_numero_documento") val usuarioNumeroDocumento: String,
    @SerializedName("usuario_tipo_documento") val usuarioTipoDocumento: String,
    @SerializedName("usuario_genero") val usuarioGenero: String,
    @SerializedName("usuario_direccion") val usuarioDireccion: String,
    @SerializedName("usuario_rol_principal") val usuarioRolPrincipal: String,
    @SerializedName("usuario_status") val usuarioStatus: String,
    @SerializedName("usuario_estado") val usuarioEstado: String,
    @SerializedName("usuario_fecha_registro") val usuarioFechaRegistro: String,
    @SerializedName("usuario_merkash") val usuarioMerkash: String,
    @SerializedName("usuario_puntos") val usuarioPuntos: String,
    @SerializedName("usuario_id_padre") val usuarioIdPadre: String?,
    @SerializedName("municipio_id") val municipioId: String?,
    @SerializedName("usuario_ruta_img") val usuarioRutaImg: String,
    val imagen: String,
    @SerializedName("usuario_latitud") val usuarioLatitud: String?,
    @SerializedName("usuario_longitud") val usuarioLongitud: String?,
    @SerializedName("usuario_bienvenida") val usuarioBienvenida: String?,
    @SerializedName("usuario_contrasena") val usuarioContrasena: String?,
    @SerializedName("usuario_token_contrasena") val usuarioTokenContrasena: String?,
    @SerializedName("usuario_token_fecha") val usuarioTokenFecha: String?,
    @SerializedName("usuario_token_merkash") val usuarioTokenMerkash: String?,
    @SerializedName("usuario_token_merkash_fecha") val usuarioTokenMerkashFecha: String?,
    @SerializedName("usuario_terminos") val usuarioTerminos: String?,
    @SerializedName("usuario_last_login") val usuarioLastLogin: String?
)

// Retrofit API for login
private interface LoginApi {
    @POST("function-api.php?title=usuarios")
    suspend fun login(@Body data: LoginData): LoginResponse
}

// Retrofit API for register (multipart)
private interface RegisterApi {
    @Multipart
    @POST("function-api-registro.php")
    suspend fun register(
        @retrofit2.http.Part("tipo") tipo: RequestBody,
        @retrofit2.http.Part("usuario_id") usuarioId: RequestBody,
        @retrofit2.http.Part("fileimagen") fileimagen: RequestBody,
        @retrofit2.http.Part("usuario_social") usuarioSocial: RequestBody,
        @retrofit2.http.Part("usuario_social_imagen") usuarioSocialImagen: RequestBody,
        @retrofit2.http.Part("nombre") nombre: RequestBody,
        @retrofit2.http.Part("apellido") apellido: RequestBody,
        @retrofit2.http.Part("usuario_telefono") usuarioTelefono: RequestBody,
        @retrofit2.http.Part("usuario_correo") usuarioCorreo: RequestBody,
        @retrofit2.http.Part("contrasena") contrasena: RequestBody,
        @retrofit2.http.Part("token") token: RequestBody,
        @retrofit2.http.Part("usuario_numero_documento") usuarioNumeroDocumento: RequestBody
    ): RegisterSuccessResponse
}

data class RegisterSuccessResponse(val validacion: String)
data class RegisterErrorResponse(val mensaje: String)

sealed class RegisterResult {
    data class Success(val response: RegisterSuccessResponse) : RegisterResult()
    data class Failure(val message: String) : RegisterResult()
}

object LoginService {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(LoginApi::class.java)
    private val registerApi = retrofit.create(RegisterApi::class.java)

    suspend fun login(data: LoginData): LoginResult = withContext(Dispatchers.IO) {
        try {
            val response = api.login(data)
            LoginResult.Success(response)
        } catch (e: HttpException) {
            // Intentar decodificar mensaje de error no es directo aquí sin body parsing
            LoginResult.Failure("HTTP Error: ${e.code()}")
        } catch (e: Exception) {
            LoginResult.Failure("Error login: ${e.localizedMessage ?: "unknown"}")
        }
    }

    // register similar a Swift: usamos multipart, retornamos RegisterResult
    suspend fun register(data: RegisterData, title: String): RegisterResult = withContext(Dispatchers.IO) {
        try {
            // Construir RequestBody para cada campo (texto plano)
            fun rb(value: String) = value.toRequestBody("text/plain".toMediaTypeOrNull())
            val tipo = rb("normal")
            val usuarioId = rb("") // tal como en Swift
            val fileImagen = rb("")
            val usuarioSocial = rb("")
            val usuarioSocialImagen = rb("")
            val nombre = rb(data.nombre)
            val apellido = rb(data.apellido)
            val telefono = rb(data.telefono)
            val correo = rb(data.correo)
            val contrasena = rb(data.contrasena)
            val token = rb(data.token)
            val usuarioNumeroDocumento = rb("")

            val response = registerApi.register(
                tipo,
                usuarioId,
                fileImagen,
                usuarioSocial,
                usuarioSocialImagen,
                nombre,
                apellido,
                telefono,
                correo,
                contrasena,
                token,
                usuarioNumeroDocumento
            )
            RegisterResult.Success(response)
        } catch (e: HttpException) {
            RegisterResult.Failure("HTTP Error: ${e.code()}")
        } catch (e: Exception) {
            RegisterResult.Failure("Error registro: ${e.localizedMessage ?: "unknown"}")
        }
    }
}

// RegisterData matching Swift's struct
data class RegisterData(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val correo: String,
    val contrasena: String,
    val token: String
)
