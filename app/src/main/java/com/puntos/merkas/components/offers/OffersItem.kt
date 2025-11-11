package com.puntos.merkas.components.offers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.puntos.merkas.components.loaders.Loading

@Composable
fun OffersItem(
    navController: NavHostController,
    title: String,
    imageUrl: String,
    commerceId: String,
    isLoading: Boolean
) {

    var lastClickTime by remember { mutableStateOf(0L) }

    if (isLoading) {
        SkeletonOfferItem()
        return
    }

    Button(
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 1000) {
                lastClickTime = currentTime
            navController.navigate("ecommerce")
            }
        },
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box {
            // Imagen desde URL
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Loading()
                        }
                    }
                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error al cargar", color = Color.White)
                        }
                    }
                    else -> {
                        SubcomposeAsyncImageContent() // 🔥 esto dibuja la imagen cuando está lista
                    }
                }
            }


            // Gradiente inferior con título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 10.dp),
                    color = Color.White
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 20.dp, bottom = 10.dp)
                        .background(Color.White, shape = CircleShape)
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .padding(start = 1.dp, top = 3.dp, bottom = 3.dp),
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun SkeletonOfferItem() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .height(160.dp)
            .background(
                color = Color.LightGray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
    )
}