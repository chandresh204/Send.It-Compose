package chad.orionsoft.sendit.ui.main_screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.sender_audio.SenderAudio
import chad.orionsoft.sendit.ui.sender_file_select.SenderFileSelect
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.sender_video.SenderVideo
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.NavigationConstants
import kotlinx.coroutines.launch

@Composable
fun MainScreen(gotoScreen: (screenRoute: String) -> Unit) {
    val vModel = hiltViewModel<MainScreenViewModel>()
    val uiState by vModel.mainScreenState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(uiState.connectedDeviceDetails) {
        if(uiState.connectedDeviceDetails == null) {
            Toast.makeText(context, "You're not connected to any device", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(topBar = {
        AppBarWithMessage(title = "Send.It")
    }) { paddings ->
        Box(modifier = Modifier.padding(paddings)) {
            ScreenContent(modifier = Modifier.fillMaxSize()) { mData, isSelected ->
                vModel.onEvent(MainScreenEvent.OnItemSelected(mData, isSelected))
            }
            SelectionInfoUi(selectionUiModel = uiState.selectionModel, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
                sendNow = {
                    gotoScreen(NavigationConstants.NAV_TRANSFER_PROGRESS_SCREEN)
                    vModel.onEvent(MainScreenEvent.OnItemSendClick)
                })
            ConnectionButton(modifier = Modifier
                .align(Alignment.BottomStart)
                .widthIn(max = 200.dp)
                .padding(16.dp), uiState.connectedDeviceDetails
            ) { gotoScreen(NavigationConstants.NAV_CREATE_CONNECTION_SCREEN) }
        }
    }
}

@Composable
private fun SelectionInfoUi(selectionUiModel: SelectionUiModel, sendNow: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = sendNow)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column {
                Text(text = "${selectionUiModel.selectedCount} Files")
                Text(text = selectionUiModel.displaySize)
            }
            Image(
                painterResource(id = R.drawable.send_icon1_small),
                modifier = Modifier.size(50.dp),
                    contentDescription = "send")
        }
    }
}

@Composable
private fun ConnectionButton(modifier: Modifier, connectionDetails: ConnectedDeviceDetails?,
                             gotoConnect: () -> Unit) {
    Card(modifier.clickable(onClick = gotoConnect)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column {
                if(connectionDetails == null) {
                    Text(text = "Not\nConnected")
                } else {
                    Text(text = "Connected To")
                    Text(text = connectionDetails.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary)
                    Text(text = connectionDetails.serverPort.toString())
                }
            }
            Image(
                painterResource(id = R.drawable.send_icon1_small),
                modifier = Modifier.size(50.dp),
                contentDescription = "send")
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
                    SenderImagesScreen(viewFullScreen = {}) { mediaData, selected ->
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
            println("onTab Changesd >>> dasdkjaslkd")
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