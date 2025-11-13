package com.puntos.merkas.screens.merkas.tabAllies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.puntos.merkas.data.services.AlliesViewModel
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    apiKey: String,
    onMapReady: ((MapLibreMap) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    // Ciclo de vida sincronizado con Compose
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(
                    Style.Builder().fromUri("https://api.maptiler.com/maps/streets/style.json?key=$apiKey")
                ) {
                    // Notificar al callback cuando el mapa esté listo
                    onMapReady?.invoke(map)
                }
            }
        }
    )
}


@Composable
fun AlliesMapPreview(
    modifier: Modifier = Modifier,
    viewModel: AlliesViewModel,
    apiKey: String
) {
    val context = LocalContext.current
    val allies by viewModel.allies.collectAsState()
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    MapLibreView(
        modifier = modifier,
        apiKey = apiKey,
        onMapReady = { map ->
            mapRef = map
            map.getStyle { style ->
                styleLoaded = true

                // Comprobamos permisos
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    try {
                        val locationComponent = map.locationComponent
                        if (!locationComponent.isLocationComponentActivated) {
                            val options = org.maplibre.android.location.LocationComponentActivationOptions
                                .builder(context, style)
                                .build()
                            locationComponent.activateLocationComponent(options)
                        }
                        locationComponent.isLocationComponentEnabled = true
                        locationComponent.lastKnownLocation?.let {
                            val position = CameraPosition.Builder()
                                .target(LatLng(it.latitude, it.longitude))
                                .zoom(13.5)
                                .build()
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                        }
                    } catch (_: SecurityException) { }
                } else {
                    // Ubicación por defecto (Bogotá)
                    val bogota = LatLng(4.7110, -74.0721)
                    val position = CameraPosition.Builder()
                        .target(bogota)
                        .zoom(11.0)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                }
            }
        }
    )

    // 🟡 Marcadores reactivos
    LaunchedEffect(allies, mapRef, styleLoaded) {
        val map = mapRef ?: return@LaunchedEffect
        if (!styleLoaded) return@LaunchedEffect

        map.clear()

        allies.forEach { ally ->
            try {
                val lat = ally.latitud.replace(',', '.').toDouble()
                val lng = ally.longitud.replace(',', '.').toDouble()
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title(ally.nombreCompleto)
                )
            } catch (_: Exception) { }
        }
    }
}

