package chad.orionsoft.sendit.ui.sender_image

import chad.orionsoft.sendit.ui.model.MediaUiData


data class SenderImagesState(
    val images: List<MediaUiData> = mutableListOf(),
    val selectedCount: Int = 0,
   /* val selectedSize : Long = 0,
    val selectedSizeToDisplay: String = "0 B"*/
)