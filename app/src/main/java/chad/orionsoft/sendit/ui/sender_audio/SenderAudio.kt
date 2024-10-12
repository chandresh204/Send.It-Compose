package chad.orionsoft.sendit.ui.sender_audio

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        val displayList = if(uiState.isSearching) {
            uiState.searchedList
        } else uiState.audioList
        items(displayList, key = { audData ->
            audData.mediaData.id
        }) { aud ->
            MediaItemViewBar(
                modifier = Modifier.animateItemPlacement(),
                isSelected = aud.selected,
                thumbnail = aud.thumbnailBitmap,
                placeHolder = R.drawable.music_note_24px,
                title = aud.mediaData.title,
                subtitle = "${Converters.formatDuration(aud.mediaData.duration)} " +
                        Converters.formatDataString(aud.mediaData.size),
                actionIconVector = Icons.Default.PlayArrow,
                actionIconDrawableRes = null,
                onActionClick = {
                    StartActivity.openFile(context, aud.mediaData.title, aud.mediaData.uri)
                }) {
                    onSelectionChanged(aud.mediaData, !aud.selected)
                    vModel.onEvent(SenderAudioEvent.OnSelectClick(aud.mediaData.id)
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
}