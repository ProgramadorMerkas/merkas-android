package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import com.puntos.merkas.data.services.Environment.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ReferredUser(
    @SerializedName("usuario_fecha_registro") val usuario_fecha_registro: String,
    @SerializedName("usuario_nombre_completo") val usuario_nombre_completo: String,
    @SerializedName("usuario_numero_documento") val usuario_numero_documento: String,
    @SerializedName("usuario_ruta_img") val usuario_ruta_img: String,
    val concepto: String,
    @SerializedName("usuario_id") val usuario_id: String,
    @SerializedName("usuario_telefono") val usuario_telefono: String,
    @SerializedName("usuario_puntos") val usuario_puntos: String,
    @SerializedName("municipio_nombre") val municipio_nombre: String,
    @SerializedName("departamento_nombre") val departamento_nombre: String,
    @SerializedName("usuario_correo") val usuario_correo: String,
    @SerializedName("usuario_estado") val usuario_estado: String
)

data class ReferralsErrorResponse(val mensaje: String)

sealed class ReferralsResult {
    data class Success(val items: List<ReferredUser>) : ReferralsResult()
    data class Failure(val message: String) : ReferralsResult()
}

private interface ReferralsApi {
    @GET("function-api.php")
    suspend fun fetchReferrals(
        @Query("title") title: String,
        @Query("id") id: String,
        @Query("token") token: String
    ): List<ReferredUser>
}

object ReferralsService {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ReferralsApi::class.java)

    suspend fun fetchReferrals(userId: String, token: String): ReferralsResult = withContext(Dispatchers.IO) {
        try {
            val response = api.fetchReferrals(title = "usuariohijosnietos", id = userId, token = token)
            ReferralsResult.Success(response)
        } catch (e: HttpException) {
            ReferralsResult.Failure("HTTP Error: ${e.code()}")
        } catch (e: Exception) {
            ReferralsResult.Failure("Error de red: ${e.localizedMessage ?: "unknown_response"}")
        }
    }
}
