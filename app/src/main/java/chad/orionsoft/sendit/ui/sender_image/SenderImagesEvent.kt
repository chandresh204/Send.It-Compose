package chad.orionsoft.sendit.ui.sender_image

sealed class SenderImagesEvent {
    data class OnSelectClick(val id: Long) : SenderImagesEvent()
    data class OnLastVisibleItemIndexChanged(val index: Int) : SenderImagesEvent()

//    data object OnSelectAll : SenderImagesEvent()
//    data object OnClearAllSelection : SenderImagesEvent()
}