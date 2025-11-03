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
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class AlliesResponse(
    @SerialName("aliados_mapas")
    val aliadosMapas: List<AlliesProps>
)

@Serializable
data class AlliesProps(
    @SerialName("aliado_merkas_sucursal_latitud")
    var latitud: String,
    @SerialName("aliado_merkas_sucursal_longitud")
    var longitud: String,
    @SerialName("usuario_ruta_img")
    val usuarioRutaImg: String,
    val icono: String,
    @SerialName("icono_negro")
    val iconoNegro: String,
    @SerialName("usuario_nombre_completo")
    val nombreCompleto: String,
    val categoria: String,
    val profesional: Boolean,
    val ubicacion: Int,
    val color: String,
    val id: String,
    val whatsapp: String,
    val direccion: String,
    @SerialName("usuario_id")
    val usuarioId: String,
    val pines: String
)

@Serializable
data class AlliesErrorResponse(
    val mensaje: String
)

sealed class AlliesResult {
    data class Success(val allies: List<AlliesProps>) : AlliesResult()
    data class Failure(val error: String) : AlliesResult()
}

class AlliesService private constructor() {

    companion object {
        val shared: AlliesService by lazy { AlliesService() }
    }

    suspend fun getAllies(token: String): AlliesResult = withContext(Dispatchers.IO) {
        val urlString = "$baseURL/function-api.php?title=aliados_ubicacion&token=$token"

        val url = try {
            URL(urlString)
        } catch (e: Exception) {
            return@withContext AlliesResult.Failure("URL inválida")
        }

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }

            Log.e("ALLIES_URL", urlString)
            Log.e("ALLIES_RESPONSE", response)

            // Intentamos decodificar lista correcta
            runCatching {
                Json.decodeFromString(AlliesResponse.serializer(), response)
            }.onSuccess { result ->
                return@withContext AlliesResult.Success(result.aliadosMapas)
            }

            // Intentamos decodificar error
            runCatching {
                Json.decodeFromString(AlliesErrorResponse.serializer(), response)
            }.onSuccess { errorResult ->
                return@withContext AlliesResult.Failure(errorResult.mensaje)
            }

            return@withContext AlliesResult.Failure("unknown_response")

        } catch (e: Exception) {
            return@withContext AlliesResult.Failure("Error de red: ${e.localizedMessage}")
        } finally {
            connection.disconnect()
        }
    }
}

class AlliesViewModel(private val tokenStore: TokenStore? = null) : ViewModel() {

    private val _allies = MutableStateFlow<List<AlliesProps>>(emptyList())
    val allies: StateFlow<List<AlliesProps>> = _allies

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAllies(token: String, tokenStore: TokenStore? = this.tokenStore) {
        viewModelScope.launch {
            val result = AlliesService.shared.getAllies(token)
            when (result) {
                is AlliesResult.Success -> {
                    _allies.value = result.allies
                    _error.value = null
                }
                is AlliesResult.Failure -> {
                    // Si el backend señaló token_incorrecto, intentar renovar token una vez
                    if (result.error == "token_incorrecto" && tokenStore != null) {
                        // Intentar obtener nuevo token
                        val newToken = TokenService.obtenerToken(tokenStore)
                        if (newToken != null) {
                            val retry = AlliesService.shared.getAllies(newToken)
                            when (retry) {
                                is AlliesResult.Success -> {
                                    _allies.value = retry.allies
                                    _error.value = null
                                    return@launch
                                }
                                is AlliesResult.Failure -> {
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