package chad.orionsoft.sendit.ui.sender_image

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.MediaImageData


data class SenderImagesState(
    val images: MutableList<ImageUiData> = mutableListOf(),
    val selectedCount: Int = 0,
    val selectedSize : Long = 0,
    val selectedSizeToDisplay: String = "0 B"
)

data class ImageUiData(
    val imageData: MediaImageData,
    var isSelected: Boolean = false,
    val thumbnailBitmap: Bitmap?
    )