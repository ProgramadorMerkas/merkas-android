package com.puntos.merkas.components.bottomNavBar

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.puntos.merkas.screens.merkas.tabAllies.AlliesScreen
import com.puntos.merkas.screens.merkas.tabHome.HomeScreen
import com.puntos.merkas.screens.merkas.tabMenu.MenuScreen
import com.puntos.merkas.screens.merkas.tabOffers.OffersScreen
import com.puntos.merkas.screens.merkas.tabReferrals.ReferralsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
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
        ) { AlliesScreen() }
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