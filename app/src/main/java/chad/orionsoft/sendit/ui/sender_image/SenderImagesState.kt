package chad.orionsoft.sendit.ui.sender_image

import android.net.Uri

data class SenderImagesState(
    val images: List<ImageData> = emptyList()
)

data class ImageData(val id: Int, val uri: Uri, var isSelected: Boolean = false)