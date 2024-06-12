package chad.orionsoft.sendit.ui.sender_audio

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.DropDownMenuData
import chad.orionsoft.sendit.ui.components.MediaItemViewBar
import chad.orionsoft.sendit.utils.Converters

@Composable
fun SenderAudio(onBack: () -> Unit) {

    val vModel = hiltViewModel<SenderAudioViewModel>()
    val uiState by vModel.avState.collectAsState()
    val listState = rememberLazyListState()

    val visibleItemSize = remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.size } }
    val firstVisibleItem = remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }


    vModel.onEvent(SenderAudioEvent.OnLastVisibleIndexChanged(visibleItemSize.value + firstVisibleItem.value))

    Scaffold(topBar = {
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
                    vModel.onEvent(SenderAudioEvent.OnSortingChangeClicked)
            }
        )
        )
    }) {
        LazyColumn(modifier = Modifier.padding(it), state = listState) {
            items(uiState.audioList) { aud ->
                MediaItemViewBar(
                    isSelected = aud.selected,
                    thumbnailBitmap = aud.thumbnailBitmap,
                    placeHolder = R.drawable.music_icon_small,
                    title = aud.audioData.title,
                    subtitle = "${Converters.formatDuration(aud.audioData.duration)} " +
                            Converters.formatDataString(aud.audioData.size),
                    actionIconVector = Icons.Default.PlayArrow,
                    actionIconDrawableRes = null,
                    onActionClick = {  }) {
                    vModel.onEvent(SenderAudioEvent.OnSelectClick(aud.audioData.id))
                }
            }
        }
    }
}