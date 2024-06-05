package chad.orionsoft.sendit.ui.sender_image

sealed class SenderImagesEvent {
    data class OnSelectClick(val id: Int) : SenderImagesEvent()
}