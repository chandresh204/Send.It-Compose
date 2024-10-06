package chad.orionsoft.sendit.ui.sender_video

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.model.SortOrder

data class SenderVideoState(
    val videoList : List<VideoUiData> = emptyList(),
    val selectedCount : Int = 0,
//    val selectedSize: Long =0,
    val isSearching: Boolean = false,
    val searchedList: List<VideoUiData> = emptyList(),
//    val selectedSizeToDisplay: String = "0 B",
    val selectedSortOrder: SortOrder = SortOrder.ByDate
)

data class VideoUiData(
    val videoData: MediaData,
    val selected: Boolean,
    val thumbnailBitmap: Bitmap?
)