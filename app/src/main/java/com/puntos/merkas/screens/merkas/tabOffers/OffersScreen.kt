package com.puntos.merkas.screens.merkas.tabOffers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.puntosmerkas.co.R
import com.puntos.merkas.components.offers.SkeletonOfferItem
import com.puntos.merkas.components.bottomNavBar.BottomNavSpacer
import com.puntos.merkas.components.offers.OffersCategory
import com.puntos.merkas.components.offers.OffersItem
import com.puntos.merkas.data.services.OffersViewModel
import com.puntos.merkas.data.services.OffersViewModelFactory

@Composable
fun OffersScreen(
    navController: NavHostController
) {
    val offersViewModel: OffersViewModel = viewModel(
        factory = OffersViewModelFactory(LocalContext.current)
    )

    val offers by offersViewModel.offers.collectAsState(initial = emptyList())
    val error by offersViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (offers.isEmpty()) {
            offersViewModel.loadOffersWithToken()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        Text(
            "OFERTAS",
            Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            Modifier
                .padding(vertical = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Spacer(
                    Modifier
                        .width(16.dp)
                        .height(10.dp)
                )
            }
            item {
                Button(
                    onClick = {},
                    Modifier
                        .width(100.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.merkas)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                        Text(
                            "TODO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Spacer(
                    Modifier
                        .width(21.dp)
                        .padding(horizontal = 10.dp)
                        .height(45.dp)
                        .background(Color.LightGray, shape = CircleShape)
                )
            }

            item {
                OffersCategory(onClick = { })
            }

            item {
                Spacer(
                    Modifier
                        .width(16.dp)
                        .height(10.dp)
                )
            }
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center)
        }

        val showPlaceholders = offers.isEmpty()
        val placeholderCount = 5

        LazyColumn(
            Modifier.padding(horizontal = 16.dp)
        ) {
            if (showPlaceholders) {
                // Dibujamos 'placeholderCount' tarjetas skeleton desde el inicio
                items(placeholderCount) { _ ->
                    SkeletonOfferItem()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Aquí pintamos la data real (categorías -> ofertas)
                items(offers) { category ->
                    // Título de categoría
                    Text(
                        text = category.titulo,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(category.color))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Iteramos las ofertas dentro de cada categoría
                    category.data.forEach { offer ->
                        OffersItem(
                            navController = navController,
                            title = offer.miniBannerPromocion.nombreComercio,
                            imageUrl = "https://www.merkas.co/merkasbusiness/${offer.miniBannerPromocion.imagen}",
                            commerceId = offer.miniBannerPromocion.id,
                            isLoading = false
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item { BottomNavSpacer() }
        }
    }
}