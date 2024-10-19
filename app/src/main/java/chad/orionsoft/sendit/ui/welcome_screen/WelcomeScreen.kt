package chad.orionsoft.sendit.ui.welcome_screen

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.common_media_screen.PermissionType
import chad.orionsoft.sendit.ui.components.PageIndicator
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage1
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage2
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage3
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Constants

@Composable
fun WelcomeScreen(vModel : WelcomeViewModel = hiltViewModel(),
                  navTo14Permission :  () -> Unit,
                  navToHome : (ColorConstants) -> Unit) {
    val uiState by vModel.uiState.collectAsState()
    val context = LocalContext.current
    val pagerState = rememberPagerState {
        3
    }
    var currentPage by rememberSaveable {
        mutableIntStateOf(0)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vModel.onEvent(WelcomeEvents.OnPermissionObtained(it))
    }
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }
    SendItTheme(selectedAppColor = uiState.appColor) {
        Scaffold {
            Column(modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(
                    Constants.PAGE_PADDING.dp
                )
                .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Image(painterResource(id = R.drawable.sendit_icon_play),
                    // TODO make size dynamic as per screen size
                    modifier = Modifier.size(100.dp),
                    contentDescription = "AppIcon")
                Spacer(modifier = Modifier.padding(Constants.PAGE_PADDING.dp))
                HorizontalPager(state = pagerState) { index ->
                    when(index) {
                        0 -> WelcomePage1(nameText = uiState.userName, onNameUpdated =  { newName ->
                            vModel.onEvent(WelcomeEvents.OnUserNameChanged(newName))
                        }) {
                            vModel.onEvent(WelcomeEvents.OnSaveUsername)
                            currentPage = 1
                        }
                        1 -> WelcomePage2(
                            allColors = uiState.appColorList,
                            selectedColor = uiState.appColor,
                            onSaveColor = {
                                vModel.onEvent(WelcomeEvents.OnSaveAppColor)
                                currentPage = 2
                            }
                        ) { newColor ->
                            vModel.onEvent(WelcomeEvents.OnNewColorSelected(newColor))
                        }
                        2 -> WelcomePage3(nextButtonString = if(uiState.permissionType == PermissionType.NoPermission) "Allow Permissions" else "Let's Go") {
                            if(uiState.permissionType == PermissionType.NoPermission) {
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    navTo14Permission()
                                } else {
                                    permissionLauncher.launch(vModel.getAllRequiredMediaPermissionArray())
                                }
                            } else {
                                if(uiState.userName.isEmpty()) {
                                    currentPage = 0
                                    Toast.makeText(context, "Please provide username", Toast.LENGTH_SHORT).show()
                                } else {
                                    navToHome(uiState.appColor)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(Constants.PAGE_PADDING.dp))
                PageIndicator(count = 3, selected = pagerState.currentPage)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {

}