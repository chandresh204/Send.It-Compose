package chad.orionsoft.sendit.ui.sender_image

import chad.orionsoft.sendit.model.MediaImageDataQ

data class SenderImagesState(
    val images: List<ImageUiData> = emptyList()
)

data class ImageUiData(
    val imageData: MediaImageDataQ,
    var isSelected: Boolean = false)