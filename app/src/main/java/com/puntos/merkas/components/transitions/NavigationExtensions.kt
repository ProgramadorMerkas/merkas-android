package com.puntos.merkas.components.transitions

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable

data class NavTransitions(
    val enter: AnimatedContentTransitionScope<*>.() -> EnterTransition?,
    val exit: AnimatedContentTransitionScope<*>.() -> ExitTransition?,
    val popEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition?,
    val popExit: AnimatedContentTransitionScope<*>.() -> ExitTransition?
)

val defaultTransitions = NavTransitions(
    enter = { defaultEnterTransition() },
    exit = {defaultExitTransition() },
    popEnter = { defaultPopEnterTransition() },
    popExit = {defaultPopExitTransition() }
)

fun NavGraphBuilder.composableWithTransitions(
    route: String,
    transitions: NavTransitions = defaultTransitions,
    content: @Composable AnimatedContentScope.() -> Unit
) {
    composable(
        route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
        content = { content() }
    )
}