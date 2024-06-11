package chad.orionsoft.sendit.ui.sender_video

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.MediaVideoData

data class SenderVideoState(
    val videoList : List<VideoUiData> = emptyList(),
    val selectedCount : Int = 0,
    val selectedSize: Long =0,
    val selectedSizeToDisplay: String = "0 B"
)

data class VideoUiData(
    val videoData: MediaVideoData,
    val selected: Boolean,
    val thumbnailBitmap: Bitmap?
)