package com.puntos.merkas.data.services

import com.google.gson.annotations.SerializedName
import com.puntos.merkas.data.network.ApiClient.retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ===============================
// ========== MODELOS ============
// ===============================

// 🔹 ID de Ecommerce
data class EcommerceIdProps(
    @SerializedName("usuario_id") val usuarioId: String
)

sealed class EcommerceIdResult {
    data class Success(val data: EcommerceIdProps) : EcommerceIdResult()
    data class Failure(val message: String) : EcommerceIdResult()
}

// 🔹 Info general del comercio
data class EcommerceInfoProps(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("usuario_nombre_completo") val nombreCompleto: String,
    @SerializedName("usuario_ruta_img") val imagenUrl: String?,
    @SerializedName("usuario_correo") val correo: String?,
    @SerializedName("usuario_telefono") val telefono: String?,
    @SerializedName("usuario_descripcion") val descripcion: String?,
    @SerializedName("usuario_direccion") val direccion: String?,
    @SerializedName("usuario_whatsapp") val whatsapp: String?,
    @SerializedName("usuario_instagram") val instagram: String?,
    @SerializedName("usuario_facebook") val facebook: String?,
    @SerializedName("usuario_tiktok") val tiktok: String?
)

sealed class EcommerceInfoResult {
    data class Success(val data: EcommerceInfoProps) : EcommerceInfoResult()
    data class Failure(val message: String) : EcommerceInfoResult()
}

// 🔹 Productos del comercio
data class EcommerceProductsProps(
    @SerializedName("producto_id") val id: String,
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("producto_categoria_id") val categoriaId: String,
    @SerializedName("producto_fecha_registro") val fechaRegistro: String,
    @SerializedName("producto_estado") val estado: String,
    @SerializedName("producto_destacado") val destacado: String,
    @SerializedName("producto_nombre") val nombre: String,
    @SerializedName("producto_descripcion") val descripcion: String,
    @SerializedName("producto_precio_final") val precioFinal: String,
    @SerializedName("producto_ruta_img") val imagenUrl: String,
    val uri: String
)

sealed class EcommerceProductsResult {
    data class Success(val items: List<EcommerceProductsProps>) : EcommerceProductsResult()
    data class Failure(val message: String) : EcommerceProductsResult()
}

// 🔹 Galería del comercio
data class EcommerceGalleryProps(
    @SerializedName("galeria_id") val id: String,
    @SerializedName("galeria_ruta_img") val rutaImg: String,
    @SerializedName("galeria_titulo") val titulo: String?,
    @SerializedName("galeria_descripcion") val descripcion: String?,
    @SerializedName("galeria_fecha") val fecha: String?
)

sealed class EcommerceGalleryResult {
    data class Success(val items: List<EcommerceGalleryProps>) : EcommerceGalleryResult()
    data class Failure(val message: String) : EcommerceGalleryResult()
}

// 🔹 Videos promocionales
data class EcommerceVideoProps(
    @SerializedName("video_id") val id: String,
    @SerializedName("video_titulo") val titulo: String?,
    @SerializedName("video_descripcion") val descripcion: String?,
    @SerializedName("video_url") val url: String,
    @SerializedName("video_thumbnail") val thumbnail: String?
)

sealed class EcommerceVideoResult {
    data class Success(val items: List<EcommerceVideoProps>) : EcommerceVideoResult()
    data class Failure(val message: String) : EcommerceVideoResult()
}

// 🔹 Error genérico
data class EcommerceErrorResponse(val mensaje: String)

// ===============================
// ========== RETROFIT ===========
// ===============================

private interface EcommerceApi {

    @GET("function-api.php")
    suspend fun getId(
        @Query("title") title: String,
        @Query("id") id: String,
        @Query("token") token: String
    ): EcommerceIdProps

    @GET("function-api.php")
    suspend fun getProducts(
        @Query("title") title: String,
        @Query("usuario") usuario: String,
        @Query("token") token: String
    ): List<EcommerceProductsProps>

    @GET("function-api.php")
    suspend fun getEcommerceInfo(
        @Query("title") title: String,
        @Query("usuario") usuario: String,
        @Query("token") token: String
    ): EcommerceInfoProps

    @GET("function-api.php")
    suspend fun getGallery(
        @Query("title") title: String,
        @Query("usuario") usuario: String,
        @Query("token") token: String
    ): List<EcommerceGalleryProps>

    @GET("function-api.php")
    suspend fun getVideos(
        @Query("title") title: String,
        @Query("usuario") usuario: String,
        @Query("token") token: String
    ): List<EcommerceVideoProps>
}

// ===============================
// ========== SERVICIO ===========
// ===============================

object EcommerceService {

    private val api = retrofit.create(EcommerceApi::class.java)

    // cache local
    @Volatile
    private var usuarioId: String? = null

    // 🔹 Obtener ID de usuario
    private suspend fun getId(ecommerceId: String, token: String): EcommerceIdResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getId(title = "aliados", id = ecommerceId, token = token)
            usuarioId = response.usuarioId
            EcommerceIdResult.Success(response)
        } catch (e: HttpException) {
            EcommerceIdResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            EcommerceIdResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            EcommerceIdResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }

    // 🔹 Obtener productos
    suspend fun getProducts(ecommerceId: String, token: String): EcommerceProductsResult = withContext(Dispatchers.IO) {
        try {
            if (usuarioId == null) {
                when (val idResult = getId(ecommerceId, token)) {
                    is EcommerceIdResult.Failure -> return@withContext EcommerceProductsResult.Failure("Error al obtener ID: ${idResult.message}")
                    is EcommerceIdResult.Success -> usuarioId = idResult.data.usuarioId
                }
            }

            val uid = usuarioId ?: return@withContext EcommerceProductsResult.Failure("No se pudo obtener el usuario_id.")
            val response = api.getProducts(title = "productos", usuario = uid, token = token)
            EcommerceProductsResult.Success(response)
        } catch (e: HttpException) {
            EcommerceProductsResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            EcommerceProductsResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            EcommerceProductsResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }

    // 🔹 Obtener información general
    suspend fun getInfo(ecommerceId: String, token: String): EcommerceInfoResult = withContext(Dispatchers.IO) {
        try {
            if (usuarioId == null) {
                when (val idResult = getId(ecommerceId, token)) {
                    is EcommerceIdResult.Failure -> return@withContext EcommerceInfoResult.Failure("Error al obtener ID: ${idResult.message}")
                    is EcommerceIdResult.Success -> usuarioId = idResult.data.usuarioId
                }
            }

            val uid = usuarioId ?: return@withContext EcommerceInfoResult.Failure("No se pudo obtener el usuario_id.")
            val response = api.getEcommerceInfo(title = "ecommerce_info", usuario = uid, token = token)
            EcommerceInfoResult.Success(response)
        } catch (e: HttpException) {
            EcommerceInfoResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            EcommerceInfoResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            EcommerceInfoResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }

    // 🔹 Obtener galería
    suspend fun getGallery(ecommerceId: String, token: String): EcommerceGalleryResult = withContext(Dispatchers.IO) {
        try {
            if (usuarioId == null) {
                when (val idResult = getId(ecommerceId, token)) {
                    is EcommerceIdResult.Failure -> return@withContext EcommerceGalleryResult.Failure("Error al obtener ID: ${idResult.message}")
                    is EcommerceIdResult.Success -> usuarioId = idResult.data.usuarioId
                }
            }

            val uid = usuarioId ?: return@withContext EcommerceGalleryResult.Failure("No se pudo obtener el usuario_id.")
            val response = api.getGallery(title = "ecommerce_gallery", usuario = uid, token = token)
            EcommerceGalleryResult.Success(response)
        } catch (e: HttpException) {
            EcommerceGalleryResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            EcommerceGalleryResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            EcommerceGalleryResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }

    // 🔹 Obtener videos
    suspend fun getVideos(ecommerceId: String, token: String): EcommerceVideoResult = withContext(Dispatchers.IO) {
        try {
            if (usuarioId == null) {
                when (val idResult = getId(ecommerceId, token)) {
                    is EcommerceIdResult.Failure -> return@withContext EcommerceVideoResult.Failure("Error al obtener ID: ${idResult.message}")
                    is EcommerceIdResult.Success -> usuarioId = idResult.data.usuarioId
                }
            }

            val uid = usuarioId ?: return@withContext EcommerceVideoResult.Failure("No se pudo obtener el usuario_id.")
            val response = api.getVideos(title = "ecommerce_videos", usuario = uid, token = token)
            EcommerceVideoResult.Success(response)
        } catch (e: HttpException) {
            EcommerceVideoResult.Failure("Error HTTP ${e.code()}: ${e.message()}")
        } catch (e: java.net.UnknownHostException) {
            EcommerceVideoResult.Failure("Sin conexión a internet")
        } catch (e: Exception) {
            EcommerceVideoResult.Failure("Error desconocido: ${e.localizedMessage}")
        }
    }
}
