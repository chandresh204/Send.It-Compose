package chad.orionsoft.sendit.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import chad.orionsoft.sendit.ui.edit_screen.EditScreen
import chad.orionsoft.sendit.ui.sender_audio.SenderAudio
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.welcome_screen.WelcomeScreen
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
            EditScreen {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_MAIN) {
            SenderMainScreen {
                navController.navigate(it)
            }
        }
        composable(NavigationConstants.NAV_RECEIVER_MAIN) {
            ReceiverMainScreen {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_IMAGES) {
            SenderImagesScreen({
                navController.navigate(NavigationConstants.NAV_FULL_SCREEN_IMAGE)
            }) {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_FULL_SCREEN_IMAGE) {
            FullScreenImage()
        }
        composable(NavigationConstants.NAV_SENDER_AUDIO) {
            SenderAudio {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_VIDEO) {
            SenderAudio {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_FILES_SELECT) {
            SenderFileSelect()
        }
    }
}