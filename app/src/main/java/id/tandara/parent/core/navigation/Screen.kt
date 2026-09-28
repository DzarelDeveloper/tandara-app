package id.tandara.parent.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("auth/login")
    data object Home : Screen("home")
    data object Reports : Screen("reports")
    data object Permission : Screen("permission")
    data object Settings : Screen("settings")
    data object Profile : Screen("settings/profile")
    data object Privacy : Screen("settings/privacy")
    data object Faq : Screen("settings/faq")
    data object About : Screen("settings/about")
    data object ChangePassword : Screen("settings/change-password")
}
