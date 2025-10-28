/* package com.puntos.merkas.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberImeVisibility(): Boolean {
    val insets = WindowInsets.ime
    val density = LocalDensity.current
    var isImeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(insets) {
        snapshotFlow { insets.getBottom(density) }
            .collect { bottom ->
                isImeVisible = bottom > 0
            }
    }

    return isImeVisible
} */