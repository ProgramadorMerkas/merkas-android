package com.puntos.merkas.data.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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


// -----------------------------------------
// MODELOS DE DATOS
// -----------------------------------------

@Serializable
data class OffersResponse(
    val data: List<OfferCategory>
)

@Serializable
data class OfferCategory(
    val id: String? = null,
    val titulo: String,
    val icono: String? = null,
    val color: String,
    val data: List<OfferData>
)

@Serializable
data class OfferData(
    val miniBannerPromocion: MiniBannerPromocion,
    val comercio: Comercio? = null
)

@Serializable
data class MiniBannerPromocion(
    val imagen: String,
    val nombreComercio: String,
    val id: String
)

@Serializable
data class Comercio(
    val aliado_merkas_ruta_imagen_portada: String? = null,
    val facebook: String? = null,
    val youtube: String? = null,
    val website: String? = null
)

@Serializable
data class OffersErrorResponse(
    val mensaje: String? = null
)

// -----------------------------------------
// RESULTADOS POSIBLES
// -----------------------------------------

sealed class OffersResult {
    data class Success(val offers: List<OfferCategory>) : OffersResult()
    data class Failure(val error: String) : OffersResult()
}

// -----------------------------------------
// SERVICIO DE OFERTAS
// -----------------------------------------

class OffersService private constructor() {

    companion object {
        val shared: OffersService by lazy { OffersService() }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    suspend fun getOffers(token: String): OffersResult = withContext(Dispatchers.IO) {
        val url = "$baseURL/function-api.php?title=todas_ofertas&token=$token"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext OffersResult.Failure("Respuesta vacía")

            Log.e("OFFERS_URL", url)
            Log.e("OFFERS_RESPONSE", body)

            val json = Json { ignoreUnknownKeys = true }

            runCatching {
                json.decodeFromString(ListSerializer(OfferCategory.serializer()), body)
            }.onSuccess { list ->
                return@withContext OffersResult.Success(list)
            }

            // Si hay error con formato esperado
            runCatching {
                json.decodeFromString(OffersErrorResponse.serializer(), body)
            }.onSuccess { err ->
                return@withContext OffersResult.Failure(err.mensaje ?: "Error desconocido")
            }

            return@withContext OffersResult.Failure("unknown_response")

        } catch (e: Exception) {
            return@withContext OffersResult.Failure("Error de red: ${e.localizedMessage}")
        }
    }
}

// -----------------------------------------
// VIEWMODEL DE OFERTAS
// -----------------------------------------

class OffersViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    // propiedad interna para poder referenciar this.tokenStore
    private val internalTokenStore: TokenStore = tokenStore

    private val _offers = MutableStateFlow<List<OfferCategory>>(emptyList())
    val offers: StateFlow<List<OfferCategory>> = _offers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadOffersWithToken() {
        viewModelScope.launch {
            try {
                val storedToken = internalTokenStore.getToken()
                val token = storedToken ?: TokenService.obtenerToken(internalTokenStore)

                if (token != null) {
                    loadOffers(token)
                } else {
                    _error.value = "No se pudo obtener token"
                }
            } catch (e: Exception) {
                _error.value = "Error al obtener token: ${e.message}"
            }
        }
    }

    // 🔹 Carga las ofertas usando el token
    fun loadOffers(token: String) {
        viewModelScope.launch {
            val result = OffersService.shared.getOffers(token)
            when (result) {
                is OffersResult.Success -> {
                    _offers.value = result.offers
                    _error.value = null
                }

                is OffersResult.Failure -> {
                    if (result.error == "token_incorrecto") {
                        val newToken = TokenService.obtenerToken(internalTokenStore)
                        if (newToken != null) {
                            val retry = OffersService.shared.getOffers(newToken)
                            when (retry) {
                                is OffersResult.Success -> {
                                    _offers.value = retry.offers
                                    _error.value = null
                                    return@launch
                                }

                                is OffersResult.Failure -> {
                                    _error.value = retry.error
                                    return@launch
                                }
                            }
                        } else {
                            _error.value = "Error renovando token"
                            return@launch
                        }
                    }
                    _error.value = result.error
                }
            }
        }
    }
}

class OffersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val tokenStore = TokenStore(context)
        return OffersViewModel(tokenStore) as T
    }
}
