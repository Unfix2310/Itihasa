package com.example.itihasa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.itihasa.R
import com.example.itihasa.ui.theme.PlusJakartaSans
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val highlightedWord: String,
    val description: String,
    val imageRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Uncover India's Heritage",
        highlightedWord = "India's",
        description = "Discover hidden stories, complete challenges and earn badges.",
        imageRes = R.drawable.ob1 // Replace with actual onboarding images
    ),
    OnboardingPage(
        title = "Learn with Every Hunt",
        highlightedWord = "Hunt",
        description = "Every location has a story. Be the explorer.",
        imageRes = R.drawable.ob2
    ),
    OnboardingPage(
        title = "Earn Badges & Track Progress",
        highlightedWord = "Badges",
        description = "Complete hunts, earn points, and climb the leaderboard.",
        imageRes = R.drawable.ob3

    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar (Logo and Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.height(32.dp)
                )
                TextButton(onClick = onFinished) {
                    Text("Skip", color = Color.Gray, fontFamily = PlusJakartaSans)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pager for content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = onboardingPages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title with Highlighted Word
                    val annotatedTitle = buildAnnotatedString {
                        val parts = page.title.split(page.highlightedWord)
                        append(parts[0])
                        withStyle(style = SpanStyle(color = Color(0xFFE46419))) {
                            append(page.highlightedWord)
                        }
                        if (parts.size > 1) append(parts[1])
                    }

                    Text(
                        text = annotatedTitle,
                        fontSize = 30.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Illustration Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Bar (Indicators and Next Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicators
                Row {
                    repeat(onboardingPages.size) { index ->
                        val color = if (pagerState.currentPage == index) Color(0xFFE46419) else Color(0xFFFDE0D0)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(10.dp)
                                .background(color, CircleShape)
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE46419)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .widthIn(min = 120.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == onboardingPages.size - 1) "Get Started" else "Next",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
