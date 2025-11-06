package com.puntos.merkas.data.services

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

// ------------------------------
// MODELOS DE DATOS
// ------------------------------

@Serializable
data class EcommerceIdProps(
    @SerialName("usuario_id")
    val usuarioId: String
)

@Serializable
data class EcommerceProductsProps(
    @SerialName("producto_id")
    val id: String,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("producto_categoria_id")
    val categoriaId: String,
    @SerialName("producto_fecha_registro")
    val fechaRegistro: String,
    @SerialName("producto_estado")
    val estado: String,
    @SerialName("producto_destacado")
    val destacado: String,
    @SerialName("producto_nombre")
    val nombre: String,
    @SerialName("producto_descripcion")
    val descripcion: String,
    @SerialName("producto_precio_final")
    val precioFinal: String,
    @SerialName("producto_ruta_img")
    val rutaImg: String,
    val uri: String
)

@Serializable
data class EcommerceErrorResponse(
    val mensaje: String
)

@Serializable
data class EcommerceProductsResponse(
    val data: List<EcommerceProductsProps>? = null
)

// ------------------------------
// RESULTADOS POSIBLES
// ------------------------------

sealed class EcommerceResult {
    data class SuccessId(val idData: EcommerceIdProps) : EcommerceResult()
    data class SuccessProducts(val products: List<EcommerceProductsProps>) : EcommerceResult()
    data class Failure(val error: String) : EcommerceResult()
}

// ------------------------------
// SERVICIO DE ECOMMERCE
// ------------------------------

class EcommerceService private constructor() {

    companion object {
        val shared: EcommerceService by lazy { EcommerceService() }
    }

    private var usuarioId: String? = null

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    suspend fun getId(ecommerceId: String, token: String): EcommerceResult = withContext(Dispatchers.IO) {
        val url = "$baseURL/function-api.php?title=aliados&id=$ecommerceId&token=$token"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext EcommerceResult.Failure("Respuesta vacía")

            Log.e("ECOMMERCE_GET_ID_URL", url)
            Log.e("ECOMMERCE_GET_ID_RESPONSE", body)

            runCatching {
                Json.decodeFromString(EcommerceIdProps.serializer(), body)
            }.onSuccess { result ->
                usuarioId = result.usuarioId
                return@withContext EcommerceResult.SuccessId(result)
            }

            runCatching {
                Json.decodeFromString(EcommerceErrorResponse.serializer(), body)
            }.onSuccess { error ->
                return@withContext EcommerceResult.Failure(error.mensaje)
            }

            return@withContext EcommerceResult.Failure("unknown_response")

        } catch (e: Exception) {
            return@withContext EcommerceResult.Failure("Error de red: ${e.localizedMessage}")
        }
    }

    suspend fun getProducts(ecommerceId: String, token: String): EcommerceResult = withContext(Dispatchers.IO) {
        if (usuarioId == null) {
            val idResult = getId(ecommerceId, token)
            if (idResult is EcommerceResult.Failure) return@withContext idResult
        }

        val id = usuarioId ?: return@withContext EcommerceResult.Failure("No se pudo obtener el usuario_id")

        val url = "$baseURL/function-api.php?title=productos&usuario=$id&token=$token"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext EcommerceResult.Failure("Respuesta vacía")

            Log.e("ECOMMERCE_PRODUCTS_URL", url)
            Log.e("ECOMMERCE_PRODUCTS_RESPONSE", body)

            runCatching {
                Json.decodeFromString(EcommerceProductsResponse.serializer(), body)
            }.onSuccess { result ->
                val products = result.data ?: emptyList()
                return@withContext EcommerceResult.SuccessProducts(products)
            }

            runCatching {
                Json.decodeFromString(ListSerializer(EcommerceProductsProps.serializer()), body)
            }.onSuccess { list ->
                return@withContext EcommerceResult.SuccessProducts(list)
            }

            runCatching {
                Json.decodeFromString(EcommerceErrorResponse.serializer(), body)
            }.onSuccess { error ->
                return@withContext EcommerceResult.Failure(error.mensaje)
            }

            return@withContext EcommerceResult.Failure("unknown_response")

        } catch (e: Exception) {
            return@withContext EcommerceResult.Failure("Error de red: ${e.localizedMessage}")
        }
    }
}

// ------------------------------
// VIEWMODEL DE ECOMMERCE
// ------------------------------

class EcommerceViewModel(private val tokenStore: TokenStore? = null) : ViewModel() {

    private val _products = MutableStateFlow<List<EcommerceProductsProps>>(emptyList())
    val products: StateFlow<List<EcommerceProductsProps>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProducts(ecommerceId: String, token: String, tokenStore: TokenStore? = this.tokenStore) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = EcommerceService.shared.getProducts(ecommerceId, token)
            when (result) {
                is EcommerceResult.SuccessProducts -> {
                    _products.value = result.products
                    _error.value = null
                    _isLoading.value = false
                }

                is EcommerceResult.Failure -> {
                    _isLoading.value = false
                    if (result.error == "token_incorrecto" && tokenStore != null) {
                        val newToken = TokenService.obtenerToken(tokenStore)
                        if (newToken != null) {
                            val retry = EcommerceService.shared.getProducts(ecommerceId, newToken)
                            if (retry is EcommerceResult.SuccessProducts) {
                                _products.value = retry.products
                                _error.value = null
                                _isLoading.value = false
                                return@launch
                            }
                        }
                    }
                    _error.value = result.error
                }

                else -> _isLoading.value = false
            }
        }
    }
}
