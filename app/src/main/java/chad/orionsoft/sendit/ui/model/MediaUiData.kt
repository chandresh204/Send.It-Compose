package chad.orionsoft.sendit.ui.model

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.MediaData

data class MediaUiData(
    val mediaData: MediaData,
    var selected: Boolean,
    val thumbnailBitmap: Bitmap?
)