package id.tandara.parent

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.tandara.parent.core.common.SystemNotificationManager
import androidx.compose.ui.Modifier
import id.tandara.parent.core.designsystem.TandaraTheme
import id.tandara.parent.core.navigation.TandaraNavHost

open class MainActivity : ComponentActivity() {
    private var notificationDestination by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationDestination = intent.getStringExtra(SystemNotificationManager.EXTRA_DESTINATION)
        val container = (application as TandaraApplication).container

        setContent {
            val appearance by container.sessionManager.appearanceFlow.collectAsState(initial = "light")
            val systemDark = isSystemInDarkTheme()
            val isDark = when (appearance) {
                "dark" -> true
                "system" -> systemDark
                else -> false // DEFAULT FOR NEW INSTALL: "light" (Terang)
            }

            TandaraTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TandaraTheme.colors.background
                ) {
                    TandaraNavHost(container = container, notificationDestination = notificationDestination, onNotificationDestinationHandled = { notificationDestination = null })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationDestination = intent.getStringExtra(SystemNotificationManager.EXTRA_DESTINATION)
    }

    override fun onResume() {
        super.onResume()
        val container = (application as TandaraApplication).container
        container.parentRealtimeCoordinator.onAppForeground()
    }

    override fun onPause() {
        (application as TandaraApplication).container.parentRealtimeCoordinator.onAppBackground()
        super.onPause()
    }
}
