package com.puntos.merkas.components.bottomNavBar

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.puntos.merkas.screens.merkas.tabAllies.AlliesScreen
import com.puntos.merkas.screens.merkas.tabHome.HomeScreen
import com.puntos.merkas.screens.merkas.tabMenu.MenuScreen
import com.puntos.merkas.screens.merkas.tabOffers.OffersScreen
import com.puntos.merkas.screens.merkas.tabReferrals.ReferralsScreen
import com.puntos.merkas.data.services.TokenStore

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val token by tokenStore.tokenFlow.collectAsState(initial = null)

    NavHost(
        navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home",
            enterTransition = { ->
                fadeIn(animationSpec = tween(40)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            }
        ) { HomeScreen(navController) }
        composable("allies",
            enterTransition = { ->
                fadeIn(animationSpec = tween(40)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            }
        ) {
            // ✅ Evitar mostrar pantalla sin token cargado todavía
            if (token == null) {
                Text("Cargando token...")
                return@composable
            }

            // ✅ Aquí enviamos token + navController
            AlliesScreen(
                token = token!!,
                navController = navController
            )
        }
        composable("offers",
            enterTransition = { ->
                fadeIn(animationSpec = tween(40)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            }
        ) { OffersScreen(navController) }
        composable("referrals",
            enterTransition = { ->
                fadeIn(animationSpec = tween(40)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            }
        ) { ReferralsScreen(navController) }
        composable("menu",
            enterTransition = { ->
                fadeIn(animationSpec = tween(40)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(40)
                )
            }
        ) { MenuScreen(navController) }
    }
}