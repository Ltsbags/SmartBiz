package com.example.core.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.example.core.constants.AppConstants

@Composable
fun FadeAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = AppConstants.ANIM_MEDIUM)),
        exit = fadeOut(animationSpec = tween(durationMillis = AppConstants.ANIM_SHORT)),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ScaleAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = tween(durationMillis = AppConstants.ANIM_MEDIUM, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = scaleOut(animationSpec = tween(durationMillis = AppConstants.ANIM_SHORT)) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideAnimation(
    visible: Boolean,
    directionVertical: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (directionVertical) {
            slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(AppConstants.ANIM_MEDIUM)) + fadeIn()
        } else {
            slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(AppConstants.ANIM_MEDIUM)) + fadeIn()
        },
        exit = if (directionVertical) {
            slideOutVertically(targetOffsetY = { 50 }, animationSpec = tween(AppConstants.ANIM_SHORT)) + fadeOut()
        } else {
            slideOutHorizontally(targetOffsetX = { 50 }, animationSpec = tween(AppConstants.ANIM_SHORT)) + fadeOut()
        },
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun Modifier.pressScaleEffect(isPressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = AppConstants.ANIM_SHORT),
        label = "PressScale"
    )
    return this.scale(scale)
}

@Composable
fun Modifier.rippleClickable(
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        onClick = onClick
    )
}
