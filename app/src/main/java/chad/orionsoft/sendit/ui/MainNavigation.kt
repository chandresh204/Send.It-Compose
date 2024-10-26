package chad.orionsoft.sendit.ui

import android.content.Intent
import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import chad.orionsoft.sendit.ui.about_screen.AboutScreen
import chad.orionsoft.sendit.ui.main_screen.MainScreen
import chad.orionsoft.sendit.ui.permission_screen.Android14MediaPermissionScreen
import chad.orionsoft.sendit.ui.qr_connect.QrConnectScreen
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.settings.SettingsScreen
import chad.orionsoft.sendit.ui.transfer_process_screen.TransferMain
import chad.orionsoft.sendit.ui.welcome_screen.WelcomeScreen
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.NavigationConstants

@Composable
fun MainNavigation(sharedIntent : Intent?,
                   initScreen: String,
                   closeApp: () -> Unit,
                   themeChangeCallback: (ColorConstants) -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = initScreen,
        enterTransition = {
            slideIn(animationSpec = tween(durationMillis = 500), initialOffset = {
                IntOffset(1000,0)
            }) +
            fadeIn(animationSpec = tween(durationMillis = 500))
        },
        exitTransition = {
            slideOut(animationSpec = tween(durationMillis = 500), targetOffset = {
                IntOffset(-1000,0)
            }) +
            fadeOut(animationSpec = tween(durationMillis = 500))
        }) {
        composable(NavigationConstants.NAV_WELCOME_SCREEN) {
            WelcomeScreen(
                navTo14Permission = {
                navController.navigate(NavigationConstants.NAV_14_PERMISSION_SCREEN)
            }) { colorConstant ->
                navController.popBackStack(NavigationConstants.NAV_WELCOME_SCREEN, true)
                navController.navigate(NavigationConstants.NAV_HOME_SCREEN)
                themeChangeCallback(colorConstant)
            }
        }
        composable(NavigationConstants.NAV_HOME_SCREEN) {
            MainScreen(sharedIntent , gotoScreen = { route ->
                navController.navigate(route)
            }, closeApp)
        }
        composable(NavigationConstants.NAV_SETTINGS) {
            SettingsScreen(themeChangeCallBack = themeChangeCallback) {
                navController.popBackStack()
            }
        }
        composable(NavigationConstants.NAV_SENDER_IMAGES) {
            SenderImagesScreen { _ , _ ->
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
        composable(
            route = NavigationConstants.NAV_TRANSFER_PROGRESS_SCREEN +
                    "/{${NavigationConstants.NAV_ARG_TRANSFER_RECEIVING}}",
            arguments = listOf(
                navArgument(NavigationConstants.NAV_ARG_TRANSFER_RECEIVING) {
                    type = NavType.BoolType
                }
            )
        ) {
            val toReceivingScreen = it.arguments
                ?.getBoolean(NavigationConstants.NAV_ARG_TRANSFER_RECEIVING) ?: false
            TransferMain(landToReceivingTab = toReceivingScreen) {
                navController.popBackStack()
            }
        }
        composable(
            route = NavigationConstants.NAV_QR_SHOW_SCREEN
        ) {
            QrConnectScreen {
                navController.popBackStack()
            }
        }
        composable(
            route = NavigationConstants.NAV_ABOUT_SCREEN
        ) {
            AboutScreen {
                navController.popBackStack()
            }
        }

        composable(
            route = NavigationConstants.NAV_14_PERMISSION_SCREEN
        ) {
            // TODO provide color constant in arguments
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Android14MediaPermissionScreen {
                    navController.popBackStack()
                }
            } else {
                // Not required
            }
        }
    }
}