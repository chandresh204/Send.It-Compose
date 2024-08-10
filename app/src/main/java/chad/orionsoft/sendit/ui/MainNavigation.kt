package chad.orionsoft.sendit.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import chad.orionsoft.sendit.ui.create_connection_flow.ui.CreateConnectionScreen
import chad.orionsoft.sendit.ui.edit_screen.EditScreen
import chad.orionsoft.sendit.ui.main_screen.MainScreen
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.welcome_screen.WelcomeScreen
import chad.orionsoft.sendit.utils.Converters
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
            MainScreen {
                navController.navigate(NavigationConstants.NAV_CREATE_CONNECTION_SCREEN)
            }
        }
        composable(NavigationConstants.NAV_CREATE_CONNECTION_SCREEN) {
            CreateConnectionScreen { isSuccess ->
                println("Connection success: $isSuccess")
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_EDIT) {
            EditScreen {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_IMAGES) {
            SenderImagesScreen({ pathInJson ->
                val route1 = NavigationConstants.NAV_FULL_SCREEN_IMAGE + "/$pathInJson"
                navController.navigate(route1)
            }) { _ , _ ->
                navController.popBackStack()
            }
        }
        composable(
            route = NavigationConstants.NAV_FULL_SCREEN_IMAGE + "/{${NavigationConstants.FULL_SCREEN_IMG_URI}}",
            arguments = listOf( navArgument(NavigationConstants.FULL_SCREEN_IMG_URI) {
                type = NavType.StringType
            })
        ) {
            val arg = it.arguments?.getString(NavigationConstants.FULL_SCREEN_IMG_URI)
            val decodedPath = Converters.decodeUriPath(arg ?: "")
            FullScreenImage(decodedPath)
        }
    }
}