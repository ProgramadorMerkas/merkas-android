package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.puntos.merkas.data.services.Environment.BASE_URL

data class OffersProps(
    val id: String,
    val titulo: String,
    val icono: String,
    val color: String,
    val data: List<OfferData>
)

data class OfferData(
    val id: String,
    @SerializedName("mini_banner_promocion") val miniBannerPromocion: MiniBannerPromocion,
    val comercio: Comercio
)

data class MiniBannerPromocion(
    val imagen: String,
    val nombreComercio: String,
    val id: String
)

data class Comercio(
    val aliadoMerkasRutaImagenPortada: String,
    val facebook: String,
    val youtube: String,
    val website: String
)

data class OffersErrorResponse(val mensaje: String)

sealed class OffersResult {
    data class Success(val items: List<OffersProps>) : OffersResult()
    data class Failure(val message: String) : OffersResult()
}

private interface OffersApi {
    @GET("function-api.php")
    suspend fun getOffers(
        @Query("title") title: String,
        @Query("token") token: String
    ): List<OffersProps>
}

object OffersService {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(OffersApi::class.java)

    suspend fun getOffers(userId: String, token: String): OffersResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getOffers(title = "todas_ofertas", token = token)
            OffersResult.Success(response)
        } catch (e: HttpException) {
            OffersResult.Failure("HTTP Error: ${e.code()}")
        } catch (e: Exception) {
            OffersResult.Failure("Error de red: ${e.localizedMessage ?: "unknown_response"}")
        }
    }
}
