package chad.orionsoft.sendit.ui.sender_video

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.bottom_sheets.OptionSelectBottomSheet
import chad.orionsoft.sendit.ui.components.MediaItemViewGrid
import chad.orionsoft.sendit.ui.model.SortOrder
import chad.orionsoft.sendit.utils.Converters

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SenderVideo(onSelectionChanged: (MediaData, Boolean) -> Unit) {

    val vModel = hiltViewModel<SenderVideoViewModel>()
    val uiState by vModel.avState.collectAsState()

    var isSortingChangeRequest by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp)) {
        val displayList = if(uiState.isSearching) {
            uiState.searchedList
        } else uiState.videoList
        items(displayList, key = { vidData ->
            vidData.videoData.id
        }) { vid ->
            MediaItemViewGrid(
                modifier = Modifier.animateItemPlacement(),
                isSelected = vid.selected,
                thumbUri = vid.videoData.uri,
                placeHolder = R.drawable.video_icon_small,
                title = vid.videoData.title,
                subtitle = "${Converters.formatDuration(vid.videoData.duration)}\n" +
                        Converters.formatDataString(vid.videoData.size),
                actionIconVector = Icons.Default.PlayArrow,
                actionIconDrawableRes = null,
                onActionClick = {
                    StartActivity.openFile(context, vid.videoData.title, vid.videoData.uri)
                }) {
                    onSelectionChanged(vid.videoData, !vid.selected)
                    vModel.onEvent(SenderVideoEvent.OnSelectClick(vid.videoData.id))
            }
        }
    }
    if(isSortingChangeRequest) {
        OptionSelectBottomSheet(
            title = stringResource(R.string.change_sort_order),
            options = SortOrder.entries,
            selectedOption = uiState.selectedSortOrder,
            onSelectOption = { order ->
                vModel.onEvent(SenderVideoEvent.OnSortingChanged(order))
            }) {
            isSortingChangeRequest = false
        }
    }

   /* Scaffold(topBar = {
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
                    isSortingChangeRequest = true
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

    }*/
}