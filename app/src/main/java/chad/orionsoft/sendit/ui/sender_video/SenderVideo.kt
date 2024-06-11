package chad.orionsoft.sendit.ui.sender_video

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.DropDownMenuData
import chad.orionsoft.sendit.ui.components.MediaItemViewGrid
import chad.orionsoft.sendit.utils.Converters

@Composable
fun SenderVideo(onBack: () -> Unit) {

    val vModel = hiltViewModel<SenderVideoViewModel>()
    val uiState by vModel.avState.collectAsState()
 //   val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val visibleItemSize = remember { derivedStateOf { gridState.layoutInfo.visibleItemsInfo.size } }
    val firstVisibleItem = remember {
        derivedStateOf { gridState.firstVisibleItemIndex }
    }

    vModel.onEvent(SenderVideoEvent.OnLastVisibleIndexChanged(visibleItemSize.value + firstVisibleItem.value))

    Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - ${uiState.videoList.size} Files",
            message = "${uiState.selectedCount} selected, Total : ${uiState.selectedSizeToDisplay}, Free Space: 1.1 GB",
            withProgress = 0.6f,
            onBackPress = onBack,
            isProvideSearch = true,
            appBarMenu = listOf(
                DropDownMenuData(Icons.Default.Check, "Select all") {

                },
                DropDownMenuData(Icons.Default.Clear, "Clear all") {
                },
                DropDownMenuData(Icons.Default.List, "Sort By") {
                }
        ))
    }) {
        LazyVerticalGrid(state = gridState,
            modifier = Modifier.padding(it),
            columns = GridCells.Adaptive(100.dp)) {
            items(uiState.videoList) { vid ->
                MediaItemViewGrid(
                    isSelected = vid.selected,
                    thumbnailBitmap = vid.thumbnailBitmap,
                    placeHolder = R.drawable.video_icon_small,
                    title = vid.videoData.title,
                    subtitle = "${Converters.formatDuration(vid.videoData.duration)}\n" +
                            Converters.formatDataString(vid.videoData.size),
                    actionIconVector = Icons.Default.PlayArrow,
                    actionIconDrawableRes = null,
                    onActionClick = {  }) {
                    vModel.onEvent(SenderVideoEvent.OnSelectClick(vid.videoData.id))
                }
            }
        }
//        LazyColumn(modifier = Modifier.padding(it), state = listState) {
//            items(uiState.videoList) { vid ->
//                MediaItemViewGrid(
//                    isSelected = vid.selected,
//                    thumbnailBitmap = vid.thumbnailBitmap,
//                    placeHolder = R.drawable.music_icon_small,
//                    title = vid.videoData.title,
//                    subtitle = "${Converters.formatDuration(vid.videoData.duration)} " +
//                            Converters.formatDataString(vid.videoData.size),
//                    actionIconVector = Icons.Default.PlayArrow,
//                    actionIconDrawableRes = null,
//                    onActionClick = {  }) {
//                    vModel.onEvent(SenderVideoEvent.OnSelectClick(vid.videoData.id))
//                }
//            }
//        }
    }
}