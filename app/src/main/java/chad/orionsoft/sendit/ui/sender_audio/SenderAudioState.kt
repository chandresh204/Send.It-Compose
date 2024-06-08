package chad.orionsoft.sendit.ui.sender_audio

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.MediaAudioData

data class SenderAVState(
    val isAudioScreen: Boolean = true,
    val audioList : List<AudioUIData> = emptyList(),
    val selectedCount : Int = 0,
    val selectedSize: Long =0,
    val selectedSizeToDisplay: String = "0 B"
)

data class AudioUIData(
    val audioData: MediaAudioData,
    val selected: Boolean,
    val thumbnailBitmap: Bitmap?
)