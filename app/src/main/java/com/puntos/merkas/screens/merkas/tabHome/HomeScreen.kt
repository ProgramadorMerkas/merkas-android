package com.puntos.merkas.screens.merkas.tabHome

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LiveHelp
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import com.puntos.merkas.R
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.puntos.merkas.components.bottomNavBar.BottomNavSpacer
import com.puntos.merkas.components.offers.OffersCategory
import com.puntos.merkas.data.services.AlliesViewModel
import com.puntos.merkas.data.services.OffersViewModel
import com.puntos.merkas.data.services.TokenStore
import com.puntos.merkas.screens.merkas.tabAllies.MapLibreView

@SuppressLint("ContextCastToActivity")
@Composable
fun HomeScreen(
    navController: NavHostController,
    datosUsuarioViewModel: DatosUsuarioViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val datosUsuario by datosUsuarioViewModel.datosUsuario.collectAsState()
    val tokenStore = remember { TokenStore(context) }
    val alliesViewModel = remember { AlliesViewModel(tokenStore) }
    val offersViewModel = remember { OffersViewModel(tokenStore) }

    // 🔹 Variables locales para nombre, puntos y merkash guardados
    val nombreGuardado = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (token != null) {
            alliesViewModel.loadAllies(token)
            offersViewModel.loadOffers(token)
        }
        // 🟢 Leer el nombre guardado en DataStore (por si no hay datosUsuario en memoria)
        val (nombre, _) = tokenStore.getUserData()
        nombreGuardado.value = nombre
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(50.dp))

            Text(
                text = when {
                    datosUsuario != null -> "Hola, ${datosUsuario!!.usuario_nombre}"
                    nombreGuardado.value != null -> "Hola, ${nombreGuardado.value}"
                    else -> "Hola..."
                },
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "¡Gana puntos con Merkas hoy!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 30.dp)
                    .background(
                        colorResource(R.color.merkas),
                        shape = RoundedCornerShape(12.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mi Merkash",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (datosUsuario != null)
                                "$${datosUsuario!!.usuario_merkash}"
                                else "Error",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        Icon(
                            imageVector = Icons.Outlined.SentimentDissatisfied,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .padding(vertical = 35.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                Column(
                    modifier = Modifier.padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mis Puntos",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (datosUsuario != null)
                                datosUsuario!!.usuario_puntos
                            else "Error",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.SentimentDissatisfied,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(30.dp)
            )

            Text(
                "Gana más puntos",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .background(
                            Color(0xFFFF7C0D),
                            shape = RoundedCornerShape(10.dp)
                        ),
                ) {
                    Button(
                        onClick = { navController.navigate("offers") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(Modifier.height(5.dp))

                            Text(
                                "Ver todas las ofertas",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.width(15.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .background(
                            Color(0xFF05B4E0),
                            shape = RoundedCornerShape(10.dp)
                        ),
                ) {
                    Button(
                        onClick = { navController.navigate("referrals") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                "Invitar amigos",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(30.dp)
            )

            Text(
                "Ver ofertas por categoría",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            OffersCategory(
                onClick = { navController.navigate("offers") }
            )

            Spacer(
                Modifier.height(30.dp)
            )

            Text(
                "Aliados cerca de ti",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                tonalElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {

                    MapLibreView(
                        Modifier
                            .fillMaxSize(),
                        apiKey = "pJD8cJKKgMxqpoeJulK5"
                    )

                    Button(
                        onClick = { navController.navigate("allies") },
                        modifier = Modifier
                            .fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RectangleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(27.dp)
                                    .background(
                                        colorResource(R.color.merkas),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ver más",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = {
                val url = "whatsapp://send?text=¡Hola Merkas!&phone=573336012020"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                try {
                    activity?.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(activity, "WhatsApp no está instalado", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 15.dp, end = 24.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(40.dp),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .size(48.dp)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.LiveHelp,
                contentDescription = "",
                tint = colorResource(R.color.merkas)
            )
        }
    }
}
