package chad.orionsoft.sendit.ui.welcome_screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.PageIndicator
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage1
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage2
import chad.orionsoft.sendit.ui.welcome_screen.welcome_pages.WelcomePage3
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Constants

/*@Composable
fun WelcomeScreen(navToHome: () -> Unit = {}) {

    val welcomeViewModel : WelcomeViewModel = hiltViewModel()

    val uiState by welcomeViewModel.uiState.collectAsState()

    if(uiState.moveToNextScreen) {
        navToHome()
    }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(id = R.drawable.sendit_icon_play),
            contentDescription = "App Icon",
            modifier = Modifier.size(RuntimeConstants.screenWidthDp / 1.5f))
        Text(text = stringResource(id = R.string.welcome_to_send_it),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = stringResource(id = R.string.enter_name_to_continue))
        Spacer(modifier = Modifier.padding(4.dp))
        OutlinedTextField(
            leadingIcon = {
                Icon(Icons.Default.Face, contentDescription = "User")
            },
            value = uiState.userName ?: "",
            onValueChange = {
                welcomeViewModel.onEvent(WelcomeEvents.OnUserNameChanged(it))
            },
            placeholder = {
                Text(text = "e.g. John",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            },
            shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = {
//            welcomeViewModel.onEvent(WelcomeEvents.OnNextClick)
        }, enabled = !uiState.userName.isNullOrEmpty()) {
            Text(stringResource(id = R.string.next))
        }
    }
}*/

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen2(vModel : WelcomeViewModel = hiltViewModel(),
                   themeChangeCallBack : (ColorConstants) -> Unit,
                   navToHome: () -> Unit,) {
    val uiState by vModel.uiState.collectAsState()
    val pagerState = rememberPagerState {
        3
    }
    var currentPage by remember {
        mutableIntStateOf(0)
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
                        2 -> WelcomePage3(nextButtonString = "Let's Go") {
                            themeChangeCallBack(uiState.appColor)
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
    WelcomeScreen2(themeChangeCallBack = {})  {}
}