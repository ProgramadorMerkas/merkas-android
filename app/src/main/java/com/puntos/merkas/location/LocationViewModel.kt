package com.puntos.merkas.location

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation = _lastLocation.asStateFlow()

    private var alreadyLoadedOnce = false

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(getApplication())
    }

    fun loadLastLocationOnce(force: Boolean = false) {
        if (alreadyLoadedOnce && !force) return
        alreadyLoadedOnce = true

        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        _lastLocation.value = loc
                        Log.d("LocationViewModel", "getCurrentLocation success: $loc")
                    } else {
                        fusedClient.lastLocation.addOnSuccessListener { last ->
                            _lastLocation.value = last
                            Log.d("LocationViewModel", "lastLocation fallback: $last")
                        }.addOnFailureListener { e ->
                            Log.w("LocationViewModel", "lastLocation fallback failed: ${e.message}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("LocationViewModel", "getCurrentLocation failed: ${e.message}")

                    fusedClient.lastLocation.addOnSuccessListener { last ->
                        _lastLocation.value = last
                        Log.d("LocationViewModel", "lastLocation fallback2: $last")
                    }
                }
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error solicitando ubicación: ${e.message}")
            viewModelScope.launch {
                _lastLocation.value = null
            }
        }
    }

    fun resetAndReload() {
        alreadyLoadedOnce = false
        loadLastLocationOnce(force = true)
    }
}