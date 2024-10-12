package chad.orionsoft.sendit.ui.sender_audio

import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.ui.model.SortOrder

data class SenderAudioState(
    val audioList : List<MediaUiData> = emptyList(),
    val selectedCount : Int = 0,
//    val selectedSize: Long =0,
//    val selectedSizeToDisplay: String = "0 B",
    val isSearching: Boolean = false,
    val searchedList : List<MediaUiData> = emptyList(),
    val selectedSortOrder: SortOrder = SortOrder.ByDate
)