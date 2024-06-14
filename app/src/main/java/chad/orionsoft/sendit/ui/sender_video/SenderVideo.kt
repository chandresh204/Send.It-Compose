package chad.orionsoft.sendit.ui.sender_video

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import chad.orionsoft.sendit.ui.sender_audio.SenderAudioEvent
import chad.orionsoft.sendit.utils.Converters

@Composable
fun SenderVideo(onBack: () -> Unit) {

    val vModel = hiltViewModel<SenderVideoViewModel>()
    val uiState by vModel.avState.collectAsState()

    Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - ${uiState.videoList.size} Files",
            message = "${uiState.selectedCount} selected, Total : ${uiState.selectedSizeToDisplay}, Free Space: 1.1 GB",
            withProgress = 0.6f,
            onBackPress = onBack,
            isProvideSearch = true,
            appBarMenu = listOf(
                DropDownMenuData(Icons.Default.Check, "Select all") {
                    vModel.onEvent(SenderVideoEvent.OnSelectAll)
                },
                DropDownMenuData(Icons.Default.Clear, "Clear all") {
                    vModel.onEvent(SenderVideoEvent.OnClearAllSelection)
                },
                DropDownMenuData(Icons.Default.List, "Sort By") {
                    vModel.onEvent(SenderVideoEvent.OnSortingChangeClicked)
                }
        ),
            onEnteredInSearchMode = {
                vModel.onEvent(SenderVideoEvent.OnSearch(""))
            },
            onSearchQueryChanged = {
                vModel.onEvent(SenderVideoEvent.OnSearch(it))
            },
            onSearchClosed = {
                vModel.onEvent(SenderVideoEvent.OnSearch(null)) // null means exit from search
            })
    }) {
        LazyVerticalGrid(
            modifier = Modifier.padding(it),
            columns = GridCells.Adaptive(100.dp)) {
            val displayList = if(uiState.isSearching) {
                uiState.searchedList
            } else uiState.videoList
            items(displayList, key = { vidData ->
                vidData.videoData.id
            }) { vid ->
                MediaItemViewGrid(
                    isSelected = vid.selected,
                    thumbUri = vid.videoData.uri,
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
    }
}