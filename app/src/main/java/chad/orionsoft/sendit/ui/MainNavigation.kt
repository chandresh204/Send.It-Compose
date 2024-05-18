package chad.orionsoft.sendit.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import chad.orionsoft.sendit.utils.NavigationConstants

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavigationConstants.NAV_WELCOME_SCREEN) {
        composable(NavigationConstants.NAV_WELCOME_SCREEN) {
            WelcomeScreen {
                navController.popBackStack(NavigationConstants.NAV_WELCOME_SCREEN, true)
                navController.navigate(NavigationConstants.NAV_HOME_SCREEN)
            }
        }
        composable(NavigationConstants.NAV_HOME_SCREEN) {
            HomeScreen {
                navController.navigate(it)
            }
        }
        composable(NavigationConstants.NAV_EDIT) {
            EditScreen()
        }
        composable(NavigationConstants.NAV_SENDER_MAIN) {
            SenderMainScreen()
        }
    }
}