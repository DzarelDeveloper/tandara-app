package id.tandara.parent.core.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.tandara.parent.core.common.AppContainer
import id.tandara.parent.ui.auth.LoginScreen
import id.tandara.parent.ui.auth.LoginViewModel
import id.tandara.parent.ui.components.TandaraBottomNavigation
import id.tandara.parent.ui.home.HomeScreen
import id.tandara.parent.ui.home.HomeViewModel
import id.tandara.parent.ui.onboarding.OnboardingScreen
import id.tandara.parent.ui.onboarding.WelcomeScreen
import id.tandara.parent.ui.permission.PermissionScreen
import id.tandara.parent.ui.permission.PermissionViewModel
import id.tandara.parent.ui.reports.ReportsScreen
import id.tandara.parent.ui.reports.ReportsViewModel
import id.tandara.parent.ui.settings.AboutScreen
import id.tandara.parent.ui.settings.ChangePasswordScreen
import id.tandara.parent.ui.settings.FaqScreen
import id.tandara.parent.ui.settings.ParentProfileScreen
import id.tandara.parent.ui.settings.PrivacyScreen
import id.tandara.parent.ui.settings.SettingsScreen
import id.tandara.parent.ui.settings.SettingsViewModel
import id.tandara.parent.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun TandaraNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Double-tap back press handler on root destination (Home)
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val isHomeRoot = currentRoute == Screen.Home.route

    BackHandler(enabled = isHomeRoot) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct exit on Welcome or Login when they serve as the root entry screen
    val isEntryExitScreen = currentRoute == Screen.Welcome.route || currentRoute == Screen.Login.route
    BackHandler(enabled = isEntryExitScreen) {
        (context as? Activity)?.finish()
    }

    // Determine whether to show bottom navigation (Only on Home, Reports, Permission, Settings)
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Reports.route,
        Screen.Permission.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TandaraBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Cold Launch Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    sessionManager = container.sessionManager,
                    authRepository = container.authRepository,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // First-Run Welcome Screen
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onStartClick = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                    onLoginClick = {
                        // User chose "Sudah punya akun? Masuk"
                        coroutineScope.launch {
                            container.sessionManager.setOnboardingCompleted()
                        }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // 3-Page Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        // User completed onboarding or clicked "Lewati"
                        coroutineScope.launch {
                            container.sessionManager.setOnboardingCompleted()
                        }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onBackToWelcome = {
                        navController.popBackStack()
                    }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginViewModel.Factory(container.authRepository)
                )
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable(Screen.Home.route) {
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        container.authRepository,
                        container.parentRepository,
                        container.attendanceRepository,
                        container.parentRealtimeCoordinator
                    )
                )
                HomeScreen(
                    viewModel = homeVm,
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPermission = {
                        navController.navigate(Screen.Permission.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Reports Screen
            composable(Screen.Reports.route) {
                val reportsVm: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(
                        container.parentRepository,
                        container.attendanceRepository
                    )
                )
                ReportsScreen(viewModel = reportsVm)
            }

            // Permission / Leave Request Screen
            composable(Screen.Permission.route) {
                val permissionVm: PermissionViewModel = viewModel(
                    factory = PermissionViewModel.Factory(
                        container.parentRepository,
                        container.permissionRepository
                    )
                )
                PermissionScreen(viewModel = permissionVm)
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                val settingsVm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.authRepository,
                        container.sessionManager
                    )
                )
                SettingsScreen(
                    viewModel = settingsVm,
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.Privacy.route)
                    },
                    onNavigateToFaq = {
                        navController.navigate(Screen.Faq.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Read-Only Parent Profile Screen
            composable(Screen.Profile.route) {
                val settingsVm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.authRepository,
                        container.sessionManager
                    )
                )
                ParentProfileScreen(
                    viewModel = settingsVm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Sub-destinations
            composable(Screen.Privacy.route) {
                PrivacyScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Faq.route) {
                FaqScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
