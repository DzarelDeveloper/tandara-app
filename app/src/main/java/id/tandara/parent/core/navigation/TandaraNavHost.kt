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
import id.tandara.parent.ui.permission.PermissionHistoryScreen
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
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest

@Composable
fun TandaraNavHost(
    container: AppContainer,
    notificationDestination: String? = null,
    onNotificationDestinationHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val isHomeRoot = currentRoute == Screen.Home.route
    LaunchedEffect(notificationDestination, currentRoute) {
        if (notificationDestination != null && currentRoute in listOf(Screen.Home.route, Screen.Reports.route, Screen.Permission.route, Screen.Settings.route)) {
            navController.navigate(notificationDestination) { launchSingleTop = true }
            onNotificationDestinationHandled()
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    BackHandler(enabled = isHomeRoot) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
    }

    val isEntryExitScreen = currentRoute == Screen.Welcome.route || currentRoute == Screen.Login.route
    BackHandler(enabled = isEntryExitScreen) {
        (context as? Activity)?.finish()
    }

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

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onStartClick = {
                        coroutineScope.launch {
                            container.sessionManager.setOnboardingCompleted()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
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

            composable(Screen.Login.route) {
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginViewModel.Factory(
                        container.authRepository,
                        container.networkConfigManager
                    )
                )
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !container.notificationPermissionManager.hasNotificationPermission()) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

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

            composable(Screen.Reports.route) {
                val reportsVm: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(
                        container.parentRepository,
                        container.attendanceRepository,
                        container.parentRealtimeCoordinator
                    )
                )
                ReportsScreen(viewModel = reportsVm)
            }

            composable(Screen.Permission.route) {
                val permissionVm: PermissionViewModel = viewModel(
                    factory = PermissionViewModel.Factory(
                        container.parentRepository,
                        container.permissionRepository,
                        container.parentRealtimeCoordinator
                    )
                )
                PermissionScreen(
                    viewModel = permissionVm,
                    onViewAllHistory = { navController.navigate(Screen.PermissionHistory.route) }
                )
            }

            composable(Screen.PermissionHistory.route) {
                val permissionVm: PermissionViewModel = viewModel(
                    factory = PermissionViewModel.Factory(
                        container.parentRepository,
                        container.permissionRepository,
                        container.parentRealtimeCoordinator
                    )
                )
                PermissionHistoryScreen(
                    viewModel = permissionVm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                val settingsVm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.authRepository,
                        container.sessionManager,
                        container.parentRepository,
                        container.networkConfigManager
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

            composable(Screen.Profile.route) {
                val settingsVm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.authRepository,
                        container.sessionManager,
                        container.parentRepository,
                        container.networkConfigManager
                    )
                )
                ParentProfileScreen(
                    viewModel = settingsVm,
                    onBackClick = { navController.popBackStack() }
                )
            }

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
