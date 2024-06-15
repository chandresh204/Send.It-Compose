package chad.orionsoft.sendit.ui.sender_video

import chad.orionsoft.sendit.model.MediaVideoData
import chad.orionsoft.sendit.ui.model.SortOrder

data class SenderVideoState(
    val videoList : List<VideoUiData> = emptyList(),
    val selectedCount : Int = 0,
    val selectedSize: Long =0,
    val isSearching: Boolean = false,
    val searchedList: List<VideoUiData> = emptyList(),
    val selectedSizeToDisplay: String = "0 B",
    val selectedSortOrder: SortOrder = SortOrder.ByDate
)

data class VideoUiData(
    val videoData: MediaVideoData,
    val selected: Boolean,
)