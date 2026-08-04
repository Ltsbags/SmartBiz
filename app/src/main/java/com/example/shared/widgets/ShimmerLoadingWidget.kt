package com.example.shared.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@Composable
fun ShimmerLoadingWidget(
    testTag: String = "shimmer_loading_widget",
    modifier: Modifier = Modifier
) {
    DashboardShimmerLoading(testTag = testTag, modifier = modifier)
}

@Composable
fun ShimmerBox(
    width: Dp = Dp.Unspecified,
    height: Dp = 20.dp,
    shapeRadius: Dp = Dimensions.radius8,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val highlightColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    val sizeModifier = when {
        width != Dp.Unspecified -> Modifier.size(width = width, height = height)
        else -> Modifier
            .fillMaxWidth()
            .height(height)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .background(brush = brush, shape = RoundedCornerShape(shapeRadius))
    )
}

@Composable
fun ShimmerCardPlaceholder(
    height: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            ShimmerBox(width = 120.dp, height = 14.dp)
            Spacer(modifier = Modifier.height(Spacing.m))
            ShimmerBox(width = 180.dp, height = 24.dp)
            Spacer(modifier = Modifier.height(Spacing.s))
            ShimmerBox(width = 90.dp, height = 12.dp)
        }
    }
}

@Composable
fun DashboardShimmerLoading(
    testTag: String = "dashboard_shimmer_loading",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.l)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        // Welcome Banner Shimmer
        ShimmerCardPlaceholder(height = 110.dp)

        // Metrics Grid Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            ShimmerCardPlaceholder(modifier = Modifier.weight(1f))
            ShimmerCardPlaceholder(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            ShimmerCardPlaceholder(modifier = Modifier.weight(1f))
            ShimmerCardPlaceholder(modifier = Modifier.weight(1f))
        }

        // List Item Shimmers
        ShimmerBox(width = 140.dp, height = 20.dp)
        repeat(3) {
            ShimmerCardPlaceholder(height = 70.dp)
        }
    }
}
