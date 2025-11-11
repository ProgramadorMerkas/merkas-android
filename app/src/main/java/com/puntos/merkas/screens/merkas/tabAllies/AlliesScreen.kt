package com.puntos.merkas.screens.merkas.tabAllies

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.puntos.merkas.R
import kotlinx.coroutines.delay
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import androidx.navigation.NavController
import com.puntos.merkas.data.services.AlliesProps
import com.puntos.merkas.data.services.AlliesViewModel
import com.puntos.merkas.data.services.TokenStore
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap

@Composable
fun AlliesScreen(
    viewModel: AlliesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    token: String,
    navController: NavController
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado del ViewModel
    val allies by viewModel.allies.collectAsState()
    val error by viewModel.error.collectAsState()

    // Cargar aliados al entrar
    LaunchedEffect(Unit) {
        viewModel.loadAllies(token, tokenStore)
    }

    var hasPermission by remember { mutableStateOf(false) }
    var canAskAgain by remember { mutableStateOf(true) }

    // Recordar si ya intentamos solicitar el permiso para no repetir en recomposición
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    // Referencias para reactividad del mapa
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    // Función para actualizar el estado actual de los permisos
    fun refreshPermissionState() {
        hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        canAskAgain =
            activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: true
    }

    // Sincroniza los permisos al entrar en la pantalla o volver del background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Launcher para solicitar ambos permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.any { it }
        refreshPermissionState()
        permissionRequested = true
        if (!hasPermission) {
            Toast.makeText(
                context,
                "Permiso denegado. Mostrando mapa general",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Pedimos permiso si no está concedido. Ejecuta la solicitud solo una vez y tras un breve retardo para evitar el bug
    LaunchedEffect(Unit) {
        delay(300)
        refreshPermissionState()
        if (!hasPermission && !permissionRequested) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            onMapReady = { view ->
                view.getMapAsync { map ->
                    mapRef = map
                    map.getStyle { style ->
                        styleLoaded = true
                    }

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
                                locationComponent.lastKnownLocation?.let {
                                    val position = CameraPosition.Builder()
                                        .target(LatLng(it.latitude, it.longitude))
                                        .zoom(14.0)
                                        .build()
                                    map.animateCamera(
                                        CameraUpdateFactory.newCameraPosition(position)
                                    )
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

        // UI sin permisos
        AnimatedVisibility(
            visible = !hasPermission,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Mostrando mapa general", color = Color.Gray)
                    Button(
                        onClick = {
                            if (canAskAgain) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No se pudo abrir Ajustes", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.merkas)
                        )
                    ) {
                        Text("Permitir ubicación")
                    }
                }
            }
        }
    }
}