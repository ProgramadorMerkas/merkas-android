package com.puntos.merkas.screens.merkas.tabAllies

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.puntos.merkas.R
import com.puntos.merkas.data.services.AlliesViewModel
import com.puntos.merkas.data.services.TokenStore
import com.puntos.merkas.location.LocationViewModel
import com.puntos.merkas.location.LocationViewModelFactory
import kotlinx.coroutines.delay
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap

@Composable
fun AlliesScreen(
    viewModel: AlliesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    token: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val tokenStore = remember { TokenStore(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val app = (context.applicationContext as Application)
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(app)
    )

    // Estado del ViewModel
    val allies by viewModel.allies.collectAsState()
    val error by viewModel.error.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    // Recordar si ya intentamos solicitar el permiso para no repetir en recomposición
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    // Referencias para reactividad del mapa
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    var initialLocationRequestedForMap by rememberSaveable { mutableStateOf(false) }

    // Función para actualizar el estado actual de los permisos
    fun refreshPermissionState() {
        hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    // Cargar aliados al entrar
    LaunchedEffect(Unit) {
        viewModel.loadAllies(token, tokenStore)
    }

    LaunchedEffect(Unit) {
        refreshPermissionState()
        // Mostrar diálogo automáticamente si no hay permisos
        if (!hasPermission && !permissionRequested) {
            delay(400) // leve delay para evitar parpadeos al abrir
            showPermissionDialog = true
        }
        // Cargar aliados del backend
        viewModel.loadAllies(token, tokenStore)
    }

    // Sincroniza los permisos al entrar en la pantalla o volver del background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    refreshPermissionState()
                }
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    try {
                        // Apagar el componente de ubicación al salir de la pantalla
                        mapRef?.locationComponent?.isLocationComponentEnabled = false
                    } catch (_: Exception) { }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Launcher para solicitar ambos permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionRequested = true
        hasPermission = permissions.values.any { it }
        refreshPermissionState()
        if (!hasPermission) {
            Toast.makeText(context, "Permiso denegado. Mostrando mapa general.", Toast.LENGTH_SHORT).show()
        } else {
            locationViewModel.loadLastLocationOnce()
        }
    }

    // Observa la ubicación one-time desde el ViewModel
    val lastLocation by locationViewModel.lastLocation.collectAsState()

    // Cuando el LastLocation cambia y el mapa está listo, animar la cámara
    LaunchedEffect(lastLocation ,mapRef, styleLoaded) {
        val map = mapRef ?: return@LaunchedEffect
        if (!styleLoaded) return@LaunchedEffect
        val loc = lastLocation ?: return@LaunchedEffect
        try {
            val position = CameraPosition.Builder()
                .target(LatLng(loc.latitude, loc.longitude))
                .zoom(14.0)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // El mapa se muestra siempre (si no hay permisos, mapa general)
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            apiKey = "pJD8cJKKgMxqpoeJulK5",
            onMapReady = { map ->
                mapRef = map
                map.getStyle { styleLoaded = true }

                // Manejo de cámara según permisos
                if (hasPermission) {
                    map.getStyle { style ->
                        try {
                            val locationComponent = map.locationComponent
                            if (!locationComponent.isLocationComponentActivated) {
                                val options = LocationComponentActivationOptions.builder(
                                    context,
                                    style
                                ).build()
                                locationComponent.activateLocationComponent(options)
                            }

                            locationComponent.isLocationComponentEnabled = true

                            // Si aún no pedimos la ubicación one-time para centrar mapa, pedimos ahora.
                            if (!initialLocationRequestedForMap) {
                                initialLocationRequestedForMap = true
                                // Pedimos al LocationViewModel que haga una única lectura puntual
                                locationViewModel.loadLastLocationOnce()
                            }

                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val bogota = LatLng(4.7110, -74.0721)
                    val position = CameraPosition.Builder()
                        .target(bogota)
                        .zoom(11.5)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                }
            }
        )

        // 🎯 EFECTO REACTIVO: Añadir/actualizar marcadores cuando cambien los aliados
        LaunchedEffect(allies, mapRef, styleLoaded) {
            val map = mapRef ?: return@LaunchedEffect
            if (!styleLoaded) return@LaunchedEffect

            try {
                // Limpiar marcadores previos
                map.clear()

                // Agregar nuevos pines
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
            } catch (_: Exception) { }
        }

        // UI superior (permiso + botón actualizar)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasPermission) {
                Text(text = "Mostrando mapa general", color = Color.Gray)
                Button(
                    onClick = { showPermissionDialog = true },
                    modifier = Modifier.padding(top = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.merkas))
                ) {
                    Text("Permitir ubicación")
                }
            } else {
                // Botón para que el usuario decida actualizar su ubicación (one-time)
                Button(
                    onClick = {
                        // Forzar nueva lectura y centrar el mapa cuando llegue
                        locationViewModel.resetAndReload()
                    },
                    modifier = Modifier.padding(top = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.merkas))
                ) {
                    Text("Actualizar mi ubicación")
                }
            }
        }

        if (showPermissionDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permiso de ubicación") },
                text = {
                    Text(
                        "Merkas necesita acceder a tu ubicación para mostrar tu posición en el mapa " +
                                "y los aliados más cercanos, solo mientras la app está en uso."
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}