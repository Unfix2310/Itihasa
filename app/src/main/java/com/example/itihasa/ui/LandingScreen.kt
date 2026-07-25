package com.example.itihasa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.itihasa.ui.theme.PlusJakartaSans
 import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.itihasa.R

@Composable
fun LandingScreen(onStartExploring: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_temple),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Gradient Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Decorative Corner (Top Left) - Placeholder for SVG
            // You can add an Image(painterResource(R.drawable.ic_corner_decor)) here

            Spacer(modifier = Modifier.height(40.dp))

            // Logo & Sun Icon
            // Replace with painterResource(id = R.drawable.ic_logo) once you add it
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Itihasa Logo",
                tint = Color.Unspecified, // Keep original SVG colors
                modifier = Modifier.size(200.dp, 100.dp)
            )

            Text(
                text = "Explore. Learn. Preserve.",
                color = Color(0xFF311A02), // Darker, more saturated orange/terracotta
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "An interactive heritage learning platform\nempowering local communities\nthrough gamified tourism.",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Button(
                onClick = onStartExploring,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE46419)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Start Exploring",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
