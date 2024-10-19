package chad.orionsoft.sendit.ui.main_screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.common_media_screen.CommonMediaTabContent
import chad.orionsoft.sendit.ui.components.ConnectedFloatingButton
import chad.orionsoft.sendit.ui.components.NotConnectedFloatingButton
import chad.orionsoft.sendit.ui.components.ReceivingFloatingCard
import chad.orionsoft.sendit.ui.components.SendFloatingButton
import chad.orionsoft.sendit.ui.components.SendingFloatingCard
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.sender_file_select.SenderFileSelect
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.NavigationConstants
import chad.orionsoft.sendit.utils.RuntimeConstants
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun MainScreen(sharedIntent: Intent?, gotoScreen: (screenRoute: String) -> Unit) {
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

    Scaffold { paddings ->
        Box(modifier = Modifier.padding(paddings)) {
            ScreenContent(
                modifier = Modifier.fillMaxSize(),
                sharedIntent,
                onDisconnectClick = { disconnectionRequest = true },
                onMenuClick = {
                    gotoScreen(NavigationConstants.NAV_SETTINGS)
                },
                onAppIconClick = {
                    gotoScreen(NavigationConstants.NAV_ABOUT_SCREEN)
                },
                onNavTo14Permission = {
                    gotoScreen(NavigationConstants.NAV_14_PERMISSION_SCREEN)
                }
            ) { mData, isSelected ->
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
                            ConnectedFloatingButton(startLetter = it.name.first(), color = RuntimeConstants.theirColor) {
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

@Composable
private fun ScreenContent(modifier: Modifier = Modifier,
                          sharedIntent: Intent?,
                          onDisconnectClick: () -> Unit,
                          onMenuClick: () -> Unit,
                          onAppIconClick: () -> Unit,
                          onNavTo14Permission : () -> Unit,
                          onMediaSelected : (MediaData, Boolean) -> Unit) {
    val pagerState = rememberPagerState {
        4
    }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        MainAppBarWithTabs(pagerState.currentPage,
            onDisconnectClick = onDisconnectClick,
            onMenuClick = onMenuClick,
            onAppIconClick = onAppIconClick
        ) {
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
                    CommonMediaTabContent(
                        mediaType = FileType.FileTypeImage, navTo14Permission = onNavTo14Permission, onItemSelectedChanged = onMediaSelected)
                }

                1 -> {
                    CommonMediaTabContent(
                        mediaType = FileType.FileTypeAudio, navTo14Permission = onNavTo14Permission, onItemSelectedChanged = onMediaSelected)
                }

                2 -> {
                    CommonMediaTabContent(
                        mediaType = FileType.FileTypeVideo, navTo14Permission = onNavTo14Permission, onItemSelectedChanged = onMediaSelected)
                }

                3 -> {
                    SenderFileSelect(sharedIntent) { mediaData, selected ->
                        onMediaSelected(mediaData, selected)
                    }
                }
            }
        }
    }
    LaunchedEffect(sharedIntent) {
        sharedIntent?.let {
            pagerState.animateScrollToPage(3) // scroll directly to file select page if arrived from shared intent
        }
    }
}

@Composable
private fun MainAppBarWithTabs(
    selectedTab: Int,
    onDisconnectClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAppIconClick: () -> Unit,
    onTabChanged: (Int) -> Unit) {
    Row {
        IconButton(onClick = onAppIconClick) {
            Image(painterResource(id = R.drawable.sendit_icon_play), contentDescription = "Send.It")
        }
        TabRow(selectedTabIndex = selectedTab,
            modifier = Modifier.weight(1f),
            divider = {},
            indicator = {
                Box(modifier = Modifier
                    .tabIndicatorOffset(it[selectedTab])
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(CircleShape)
                    .zIndex(-1f)
                    .background(MaterialTheme.colorScheme.primaryContainer))
            }){
            IconButton(onClick = { onTabChanged(0) }) {
                Icon(painterResource(R.drawable.image_24px), contentDescription = "Photos")
            }
            IconButton(onClick = { onTabChanged(1) }) {
                Icon(painterResource(R.drawable.music_note_24px), contentDescription = "Audio")
            }
            IconButton(onClick = { onTabChanged(2) }) {
                Icon(painterResource(R.drawable.play_circle_24px), contentDescription = "Video")
            }
            IconButton(onClick = { onTabChanged(3) }) {
                Icon(painterResource(R.drawable.file_present_24px), contentDescription = "File")
            }
        }
        IconButton(onClick = onDisconnectClick) {
            Icon(painter = painterResource(id = R.drawable.power_settings_new_24px),
                contentDescription = "Close")
        }
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        MainAppBarWithTabs(selectedTab = 1, {}, {}, {}) {
            
        }
    }
}