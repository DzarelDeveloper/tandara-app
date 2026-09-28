package id.tandara.parent.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.navigation.Screen
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.domain.repository.AuthRepository
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.ui.components.TandaraLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.92f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smooth entrance animation
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Read session & onboarding state concurrently with brief display
        val hasCompletedOnboarding = sessionManager.hasCompletedOnboardingFlow.first()
        val sessionUser = sessionManager.sessionFlow.first()

        // Wait slightly for fluid UX transition without artificial delay
        delay(750)

        val destination = when {
            !hasCompletedOnboarding -> Screen.Welcome.route
            sessionUser.isAuthenticated -> when (authRepository.validateSession()) {
                is ApiResult.Success, is ApiResult.BackendUnavailable -> Screen.Home.route
                else -> Screen.Login.route
            }
            else -> Screen.Login.route
        }

        onNavigate(destination)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("screen_splash")
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        // Center: Tandara Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(horizontal = 32.dp)
        ) {
            TandaraLogo(size = 84.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tandara",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pantau Langkah, Dukung Masa Depan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SecondaryText
                )
            )
        }

        // Bottom: Subtle loading indicator
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .size(24.dp)
                .testTag("splash_loading_indicator"),
            color = PrimaryBlue,
            strokeWidth = 2.5.dp
        )
    }
}
