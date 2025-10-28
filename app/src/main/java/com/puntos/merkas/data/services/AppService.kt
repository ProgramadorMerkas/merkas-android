package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import com.puntos.merkas.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Query

// Este archivo Swift sólo contenía tipos vacíos y enum, aquí se deja el equivalente en Kotlin para futura ampliación.

data class AppVersionProps(
    val versionActual: String,
    val versionMinima: String,
    val urlActualizacion: String,
    val mensaje: String
)

data class AppVersionResponse(
    @SerializedName("version") val version: AppVersionProps
)

sealed class AppVersionResult {
    data class Success(val props: AppVersionProps) : AppVersionResult()
    data class Failure(val message: String) : AppVersionResult()
}

private interface AppApi {
    @GET("function-api.php")
    suspend fun getAppVersion(
        @Query("title") title: String,
        @Query("token") token: String
    ): AppVersionResponse
}

object AppService {
    private val api = ApiClient.retrofit.create(AppApi::class.java)

    suspend fun getAppVersion(token: String): AppVersionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getAppVersion("version_app", token)
            AppVersionResult.Success(response.version)
        } catch (e: HttpException) {
            AppVersionResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            AppVersionResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            AppVersionResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }
}
