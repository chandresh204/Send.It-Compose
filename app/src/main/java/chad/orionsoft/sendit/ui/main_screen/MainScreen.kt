package chad.orionsoft.sendit.ui.main_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.sender_audio.SenderAudio
import chad.orionsoft.sendit.ui.sender_file_select.SenderFileSelect
import chad.orionsoft.sendit.ui.sender_image.SenderImagesScreen
import chad.orionsoft.sendit.ui.sender_video.SenderVideo
import chad.orionsoft.sendit.ui.theme.SendItTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val vModel = hiltViewModel<MainScreenViewModel>()
    val uiState by vModel.mainScreenState.collectAsState()

    Scaffold(topBar = {
        AppBarWithMessage(title = "Send.It")
    },
        floatingActionButton = {
            SelectionInfoUi(uiState.selectionModel)
        }
        ) { paddings ->
        Box(modifier = Modifier.padding(paddings)) {
            ScreenContent(modifier = Modifier.fillMaxSize()) { mData, isSelected ->
                vModel.onEvent(MainScreenEvent.OnItemSelected(mData, isSelected))
            }
        }
    }
}

@Composable
private fun SelectionInfoUi(selectionUiModel: SelectionUiModel) {
    Card {
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

@Preview
@Composable
private fun Preview() {
    SendItTheme {
        MainScreen()
    }
}