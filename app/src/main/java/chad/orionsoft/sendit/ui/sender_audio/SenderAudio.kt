package chad.orionsoft.sendit.ui.sender_audio

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.bottom_sheets.OptionSelectBottomSheet
import chad.orionsoft.sendit.ui.components.MediaItemViewBar
import chad.orionsoft.sendit.ui.model.SortOrder
import chad.orionsoft.sendit.utils.Converters

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SenderAudio(onSelectionChanged: (MediaData, Boolean) -> Unit) {

    val vModel = hiltViewModel<SenderAudioViewModel>()
    val uiState by vModel.avState.collectAsState()

    var isSortingChangeRequest by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    LazyColumn {
        val displayList = if(uiState.isSearching) {
            uiState.searchedList
        } else uiState.audioList
        items(displayList, key = { audData ->
            audData.audioData.id
        }) { aud ->
            MediaItemViewBar(
                modifier = Modifier.animateItemPlacement(),
                isSelected = aud.selected,
                thumbUri = aud.audioData.thumbnail,
                placeHolder = R.drawable.music_note_24px,
                title = aud.audioData.title,
                subtitle = "${Converters.formatDuration(aud.audioData.duration)} " +
                        Converters.formatDataString(aud.audioData.size),
                actionIconVector = Icons.Default.PlayArrow,
                actionIconDrawableRes = null,
                onActionClick = {
                    StartActivity.openFile(context, aud.audioData.title, aud.audioData.uri)
                }) {
                    onSelectionChanged(aud.audioData, !aud.selected)
                    vModel.onEvent(SenderAudioEvent.OnSelectClick(aud.audioData.id)
                )
            }
        }
    }

    if(isSortingChangeRequest) {
        OptionSelectBottomSheet(
            title = stringResource(R.string.change_sort_order),
            options = SortOrder.entries,
            selectedOption = uiState.selectedSortOrder,
            onSelectOption = { order ->
                vModel.onEvent(SenderAudioEvent.OnSortingChanged(order))
            }) {
            isSortingChangeRequest = false
        }
    }

    /*Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - ${uiState.audioList.size} Files",
            message = "${uiState.selectedCount} selected, Total : ${uiState.selectedSizeToDisplay}, Free Space: 1.1 GB",
            withProgress = 0.6f,
            onBackPress = onBack,
            isProvideSearch = true,
            appBarMenu = listOf(
                DropDownMenuData(Icons.Default.Check, "Select all") {
                    vModel.onEvent(SenderAudioEvent.OnSelectAll)
                },
                DropDownMenuData(Icons.Default.Clear, "Clear all") {
                    vModel.onEvent(SenderAudioEvent.OnClearAllSelection)
                },
                DropDownMenuData(Icons.Default.List, "Sort By") {
// vModel.onEvent(SenderAudioEvent.OnSortingChangeClicked)
                    isSortingChangeRequest = true
                }
            ),
            onEnteredInSearchMode = {
                vModel.onEvent(SenderAudioEvent.OnSearch(""))
            },
            onSearchQueryChanged = {
                vModel.onEvent(SenderAudioEvent.OnSearch(it))
            },
            onSearchClosed = {
                vModel.onEvent(SenderAudioEvent.OnSearch(null)) // null means exit from search
            }

        )
    }) {

    }*/
}