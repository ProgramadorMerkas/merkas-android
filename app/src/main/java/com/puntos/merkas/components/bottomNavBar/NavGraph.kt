package com.puntos.merkas.components.bottomNavBar

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.puntos.merkas.components.transitions.composableWithTransitions
import com.puntos.merkas.screens.merkas.tabAllies.AlliesScreen
import com.puntos.merkas.screens.merkas.tabHome.HomeScreen
import com.puntos.merkas.screens.merkas.tabMenu.MenuScreen
import com.puntos.merkas.screens.merkas.tabOffers.OffersScreen
import com.puntos.merkas.screens.merkas.tabReferrals.ReferralsScreen
import com.puntos.merkas.data.services.TokenStore
import com.puntos.merkas.screens.auth.IntroScreen
import com.puntos.merkas.screens.auth.login.LoginScreen
import com.puntos.merkas.screens.auth.signup.SignUpScreen
import com.puntos.merkas.screens.merkas.tabHome.DatosUsuarioViewModel
import com.puntos.merkas.screens.merkas.tabOffers.EcommerceScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val datosUsuarioViewModel: DatosUsuarioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "intro"
    ) {
        composableWithTransitions("intro") {
            IntroScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("signup") }
            )
        }
        composableWithTransitions("login") {
            LoginScreen(
                navController = navController,
                homeScreen = { navController.navigate("home") {
                    popUpTo("intro") { inclusive = true }
                } },
                datosUsuarioViewModel = datosUsuarioViewModel
            )
        }
        composableWithTransitions("signup") {
            SignUpScreen(
                navController = navController,
                homeScreen = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composableWithTransitions("home") {
            HomeNavigation(
                parentNavController = navController,
                datosUsuarioViewModel = datosUsuarioViewModel)
        }
    }
}

@Composable
fun HomeNavigation(
    parentNavController: NavHostController,
    datosUsuarioViewModel: DatosUsuarioViewModel
) {
    val childNavController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
        bottomBar = { BottomNavBar(navController = childNavController) }
    ) { padding ->
        NavGraph(
            navController = childNavController,
            parentNavController = parentNavController,
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                start = padding.calculateStartPadding(LayoutDirection.Ltr),
                end = padding.calculateEndPadding(LayoutDirection.Ltr)
            ),
            datosUsuarioViewModel = datosUsuarioViewModel
        )
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    parentNavController: NavHostController,
    modifier: Modifier = Modifier,
    datosUsuarioViewModel: DatosUsuarioViewModel
) {
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
        ) { HomeScreen(
            navController = navController,
            datosUsuarioViewModel = datosUsuarioViewModel
        ) }
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
                fadeIn(animationSpec = tween(40)
                )
            },
            exitTransition = { ->
                fadeOut(animationSpec = tween(40)
                )
            }
        ) { OffersScreen(navController)
        }
        composable("ecommerce",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, // 👈 entra desde la izquierda
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, // 👈 sale hacia la izquierda
                    animationSpec = tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, // 👈 entra desde la izquierda
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, // 👈 sale hacia la izquierda
                    animationSpec = tween(500)
                )
            },
        ) { EcommerceScreen(navController)
        }
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
        ) { MenuScreen(
            navController = navController,
            parentNavController = parentNavController,
            tokenStore = tokenStore
        ) }
    }
}