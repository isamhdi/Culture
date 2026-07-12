package com.culture.tracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.culture.tracker.ui.navigation.CultureNavHost
import com.culture.tracker.ui.onboarding.OnboardingScreen
import com.culture.tracker.ui.settings.SettingsViewModel
import com.culture.tracker.ui.splash.SplashScreen
import com.culture.tracker.ui.theme.CultureTheme
import org.koin.androidx.compose.koinViewModel

private enum class AppStage { SPLASH, ONBOARDING, MAIN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            var showSplash by remember { mutableStateOf(true) }
            val stage = when {
                showSplash -> AppStage.SPLASH
                !settings.hasCompletedOnboarding -> AppStage.ONBOARDING
                else -> AppStage.MAIN
            }

            CultureTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = stage, label = "appStage") { currentStage ->
                        when (currentStage) {
                            AppStage.SPLASH -> SplashScreen(onFinished = { showSplash = false })
                            AppStage.ONBOARDING -> OnboardingScreen(onDone = { name -> settingsViewModel.completeOnboarding(name) })
                            AppStage.MAIN -> CultureNavHost()
                        }
                    }
                }
            }
        }
    }
}
