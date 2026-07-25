package com.example.itihasa.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.itihasa.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1. Reduced delay for faster transition
        delay(800)

        // 2. Zoom Phase: One fast, smooth blast-through
        // We use EaseInQuart so it starts slightly slow and gets very fast
        scale.animateTo(50f, animationSpec = tween(800, easing = EaseInQuart))
        
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Itihasa Logo",
            modifier = Modifier
                .size(250.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    // Fade out as it gets very large to reveal landing screen
                    this.alpha = if (scale.value > 5f) {
                        (1f - (scale.value - 5f) / 15f).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                    transformOrigin = TransformOrigin(0.55f, 0.5f)
                }
        )
    }
}
