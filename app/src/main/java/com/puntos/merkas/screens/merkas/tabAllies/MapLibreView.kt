package com.puntos.merkas.screens.merkas.tabAllies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    apiKey: String,
    onMapReady: ((MapView) -> Unit)? = null
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
                ) { style ->
                    try {
                        val hasFineLocation = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        val hasCoarseLocation = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation || hasCoarseLocation) {
                            // Si tiene permiso -> activar componente de ubicación
                        val locationComponent = map.locationComponent
                        val activationOptions = LocationComponentActivationOptions
                            .builder(context, style)
                            .useDefaultLocationEngine(true)
                            .build()
                        locationComponent.activateLocationComponent(activationOptions)
                        locationComponent.isLocationComponentEnabled = true

                        // Centrar cámara en la ubicación del usuario si está disponible
                        locationComponent.lastKnownLocation?.let {
                            val position = CameraPosition.Builder()
                                .target(
                                    LatLng(it.latitude, it.longitude))
                                .zoom(14.0)
                                .build()
                            map.animateCamera(
                                CameraUpdateFactory.newCameraPosition(position))
                        }
                        } else {
                            val defaultPosition = CameraPosition.Builder()
                                .target(LatLng(4.7110, -74.0721))
                                .zoom(12.0)
                                .build()
                            map.moveCamera(CameraUpdateFactory.newCameraPosition(defaultPosition))
                        }

                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }

                    // Callback
                    onMapReady?.invoke(mapView)
                }
            }
        }
    )
}


















