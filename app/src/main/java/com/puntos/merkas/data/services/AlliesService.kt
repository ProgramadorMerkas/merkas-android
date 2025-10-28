package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import com.puntos.merkas.data.network.ApiClient
import com.puntos.merkas.data.network.ApiClient.retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// environment.BASE_URL import
import com.puntos.merkas.data.services.Environment.BASE_URL


// Models
data class AlliesResponse(
    @SerializedName("aliados_mapas") val aliadosMapas: List<AlliesProps>
)

data class AlliesProps(
    @SerializedName("aliado_merkas_sucursal_latitud") val latitud: String,
    @SerializedName("aliado_merkas_sucursal_longitud") val longitud: String,
    @SerializedName("usuario_ruta_img") val usuarioRutaImg: String,
    val icono: String,
    @SerializedName("icono_negro") val iconoNegro: String,
    @SerializedName("usuario_nombre_completo") val nombreCompleto: String,
    val categoria: String,
    val profesional: Boolean,
    val ubicacion: Int,
    val color: String,
    val id: String,
    val whatsapp: String,
    val direccion: String,
    @SerializedName("usuario_id") val usuarioId: String,
    val pines: String
)

data class AlliesErrorResponse(
    val mensaje: String
)

// Result sealed class
sealed class AlliesResult {
    data class Success(val items: List<AlliesProps>) : AlliesResult()
    data class Failure(val message: String) : AlliesResult()
}

// Retrofit API
private interface AlliesApi {
    @GET("function-api.php?title=aliados_ubicacion&token=") // token se concatenará en runtime
    suspend fun getAlliesFullUrl(): AlliesResponse // not used directly
}

// Service
    private val api = retrofit.create(AlliesApi2::class.java)

    // Usamos Retrofit dinámico con full URL construcción en OkHttp request vía retrofit.create no es ideal para query con token dinámico,
    // así construimos una llamada con Retrofit + manual request via okhttp cuando es necesario, pero para simplicidad usaremos Retrofit + custom path:
    private interface AlliesApi2 {
        @GET("function-api.php")
        suspend fun getAllies(
            @retrofit2.http.Query("title") title: String,
            @retrofit2.http.Query("token") token: String
        ): AlliesResponse
    }

    suspend fun getAllies(token: String): AlliesResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllies("aliados_ubicacion", token)
            AlliesResult.Success(response.aliadosMapas)
        } catch (e: HttpException) {
            AlliesResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            AlliesResult.Failure("No hay conexión a internet")
        } catch (e: Exception) {
            AlliesResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }
