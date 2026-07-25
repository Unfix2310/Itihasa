package com.example.itihasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itihasa.ui.AuthScreen
import com.example.itihasa.ui.LandingScreen
import com.example.itihasa.ui.MainDashboard
import com.example.itihasa.ui.OnboardingScreen
import com.example.itihasa.ui.SplashScreen
import com.example.itihasa.ui.theme.ItihasaTheme
import com.example.itihasa.ui.viewmodel.HomeViewModel
import com.example.itihasa.di.HuntEngineViewModelFactory
import com.example.itihasa.engine.ui.compose.HuntListScreen
import com.example.itihasa.engine.ui.compose.HuntScreen
import com.example.itihasa.engine.ui.HuntCatalogViewModel
import com.example.itihasa.engine.ui.HuntViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ItihasaTheme {
                val auth = FirebaseAuth.getInstance()
                val homeViewModel: HomeViewModel = viewModel()
                val huntFactory = remember { HuntEngineViewModelFactory() }
                val huntCatalogViewModel: HuntCatalogViewModel = viewModel(factory = huntFactory)
                val huntViewModel: HuntViewModel = viewModel(factory = huntFactory)
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedSiteId by remember { mutableStateOf("") }
                var selectedHuntId by remember { mutableStateOf("") }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(onAnimationFinished = {
                            if (auth.currentUser != null) {
                                currentScreen = "dashboard_ready"
                            } else {
                                currentScreen = "landing"
                            }
                        })
                    }
                    "landing" -> {
                        LandingScreen(onStartExploring = {
                            currentScreen = "onboarding"
                        })
                    }
                    "onboarding" -> {
                        OnboardingScreen(onFinished = {
                            currentScreen = "auth"
                        })
                    }
                    "auth" -> {
                        AuthScreen(onLoginSuccess = { _, _ ->
                            currentScreen = "home"
                        })
                    }
                    "home" -> {
                        MainDashboard(
                            homeViewModel = homeViewModel,
                            onOpenSite = { siteId ->
                                selectedSiteId = siteId
                                currentScreen = "hunt_list"
                            },
                            onLogout = {
                                currentScreen = "auth"
                            }
                        )
                    }
                    "dashboard_ready" -> {
                        MainDashboard(
                            homeViewModel = homeViewModel,
                            onOpenSite = { siteId ->
                                selectedSiteId = siteId
                                currentScreen = "hunt_list"
                            },
                            onLogout = {
                                currentScreen = "auth"
                            }
                        )
                    }
                    "hunt_list" -> {
                        HuntListScreen(
                            siteId = selectedSiteId,
                            onBack = { currentScreen = "home" },
                            onStartHunt = { huntId ->
                                selectedHuntId = huntId
                                currentScreen = "hunt"
                            },
                            viewModel = huntCatalogViewModel
                        )
                    }
                    "hunt" -> {
                        HuntScreen(
                            siteId = selectedSiteId,
                            huntId = selectedHuntId,
                            onBack = { currentScreen = "hunt_list" },
                            viewModel = huntViewModel
                        )
                    }
                }
            }
        }
    }
}
