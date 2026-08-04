package com.example.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DimensionsSystem(
    // Border Radius System (Prompt #2 Specification)
    val radius8: Dp = 8.dp,
    val radius12: Dp = 12.dp,
    val radius16: Dp = 16.dp,
    val radius20: Dp = 20.dp,
    val radius24: Dp = 24.dp,
    val radius32: Dp = 32.dp,

    // Shadow System
    val shadowSmall: Dp = 2.dp,
    val shadowMedium: Dp = 4.dp,
    val shadowLarge: Dp = 8.dp,

    // Component Sizing
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 50.dp,
    val inputHeight: Dp = 56.dp,
    val topBarHeight: Dp = 64.dp,
    val bottomNavHeight: Dp = 80.dp,

    // Aliases
    val cardCornerRadius: Dp = 16.dp,
    val buttonCornerRadius: Dp = 12.dp,
    val chipCornerRadius: Dp = 8.dp,
    val dialogCornerRadius: Dp = 24.dp,
    val cardElevation: Dp = 2.dp,
    val modalElevation: Dp = 8.dp,
    val fabElevation: Dp = 6.dp,

    val strokeWidthThin: Dp = 1.dp,
    val strokeWidthThick: Dp = 2.dp
)

val LocalDimensions = compositionLocalOf { DimensionsSystem() }

val Dimensions: DimensionsSystem
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current
