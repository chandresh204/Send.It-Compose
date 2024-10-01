package chad.orionsoft.sendit.ui.main_screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.components.ConnectedFloatingButton
import chad.orionsoft.sendit.ui.components.NotConnectedFloatingButton
import chad.orionsoft.sendit.ui.components.ReceivingFloatingCard
import chad.orionsoft.sendit.ui.components.SendFloatingButton
import chad.orionsoft.sendit.ui.components.SendingFloatingCard
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.sender_audio.SenderAudio
import chad.orionsoft.sendit.ui.sender_file_select.SenderFileSelect
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.sender_video.SenderVideo
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.NavigationConstants
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(gotoScreen: (screenRoute: String) -> Unit) {
    val vModel = hiltViewModel<MainScreenViewModel>()
    val uiState by vModel.mainScreenState.collectAsState()
    var disconnectionRequest by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.connectedDeviceDetails) {
        if(uiState.connectedDeviceDetails == null) {
            Toast.makeText(context, "You're not connected to any device", Toast.LENGTH_LONG).show()
        }
    }

    if(disconnectionRequest) {
        BottomSheetWith2Buttons(
            title = if (uiState.connectedDeviceDetails != null) "Do you want to disconnect?" else "Exit the App?",
            positiveButton = {
                TextWithIcon(Icons.Default.Done, text = "Yes")
            },
            negativeButton = {
                TextWithIcon(Icons.Default.Clear, text = "No")
            },
            positiveButtonClick = {
                disconnectionRequest = false
                if(uiState.connectedDeviceDetails != null) {
                    vModel.onEvent(MainScreenEvent.DisconnectNow)
                } else {
                    exitProcess(0)
                }
            },
            negativeButtonClick = {
                disconnectionRequest = false
            }) {
            disconnectionRequest = false
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(stringResource(R.string.app_name))
        },
            actions = {
                IconButton(onClick = {
                    disconnectionRequest = true
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Disconnect")
                }
            }
        )
    }) { paddings ->
        Box(modifier = Modifier.padding(paddings)) {
            ScreenContent(modifier = Modifier.fillMaxSize()) { mData, isSelected ->
                vModel.onEvent(MainScreenEvent.OnItemSelected(mData, isSelected))
            }
            Column(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    horizontal = Constants.PAGE_PADDING.dp
                )) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)) {
                    if(uiState.connectedDeviceDetails == null) {
                        NotConnectedFloatingButton {
                            gotoScreen(NavigationConstants.NAV_QR_SHOW_SCREEN)
                        }
                    } else {
                        uiState.connectedDeviceDetails?.let {
                            ConnectedFloatingButton(startLetter = it.name.first(), color = it.color) {
                                // TODO Show Connection statistics
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if(uiState.selectionModel.selectedCount > 0) {
                        uiState.selectionModel.let {
                            SendFloatingButton(
                                selectedCount = it.selectedCount,
                                sizeText = it.displaySize,
                                isWarning = it.isWarning) {
                                if(uiState.connectedDeviceDetails == null) {
                                    gotoScreen(NavigationConstants.NAV_QR_SHOW_SCREEN)
                                } else {
                                    gotoScreen(NavigationConstants.NAV_TRANSFER_PROGRESS_SCREEN + "/false")
                                    vModel.onEvent(MainScreenEvent.OnItemSendClick)
                                }
                            }
                        }
                    }
                }
                if(uiState.sendingFlowInfo.isVisible) {
                    uiState.sendingFlowInfo.let {
                        SendingFloatingCard(
                            modifier = Modifier.padding(bottom = 8.dp),
                            sendingSpeed = it.transferRate,
                            sendingSpeedUnit = it.transferRateUnit,
                            currentItem = it.transferItemDetail.fold("") { acc: String, s: String ->
                                "$acc $s"
                            },
                            progressUpdate = { it.currentItemProgress }
                        ) {
                            gotoScreen(
                                NavigationConstants.NAV_TRANSFER_PROGRESS_SCREEN + "/false"
                            )
                        }
                    }
                }
                if(uiState.receivingFlowInfo.isVisible) {
                    uiState.receivingFlowInfo.let {
                        ReceivingFloatingCard(
                            modifier = Modifier.padding(bottom = 8.dp),
                            receivingSpeed = it.transferRate,
                            receivingSpeedUnit = it.transferRateUnit,
                            currentItem = it.transferItemDetail.fold("") { acc: String, s: String ->
                                "$acc $s"
                            },
                            { it.currentItemProgress },
                            received = TransferProcessDomain.totalReceivedCount
                        ) {
                            gotoScreen(NavigationConstants.NAV_TRANSFER_PROGRESS_SCREEN + "/true")
                        }
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenContent(modifier: Modifier = Modifier, onMediaSelected : (MediaData, Boolean) -> Unit) {
    val pagerState = rememberPagerState {
        4
    }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        Tabs(pagerState.currentPage) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(it)
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = modifier
            ) {
            when (it) {
                0 -> {
                    SenderImagesScreen { mediaData, selected ->
                        onMediaSelected(mediaData, selected)
                    }
                }

                1 -> {
                    SenderAudio { mediaData, selected ->
                        onMediaSelected(mediaData, selected)
                    }
                }

                2 -> {
                    SenderVideo { mediaData, selected ->
                        onMediaSelected(mediaData, selected)
                    }
                }

                3 -> {
                    SenderFileSelect()
                }
            }
        }
    }
}

@Composable
private fun Tabs(selectedTab: Int, onTabChanged: (Int) -> Unit) {
    TabRow(selectedTabIndex = selectedTab){
        TabText(title = "Images") {
            onTabChanged(0)
        }
        TabText(title = "Audio") {
            onTabChanged(1)
        }
        TabText(title = "Video") {
            onTabChanged(2)
        }
        TabText(title = "Select") {
            onTabChanged(3)
        }
    }
}

@Composable
private fun TabText(title: String, onClick: () -> Unit) {
    Text(text = title,
        textAlign = TextAlign.Center,
        modifier = Modifier.clickable {
            println("Inside tab click >>>>")
            onClick()
        })
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    SendItTheme {

    }
}