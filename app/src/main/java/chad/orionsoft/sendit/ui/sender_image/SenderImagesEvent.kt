package chad.orionsoft.sendit.ui.sender_image

import chad.orionsoft.sendit.ui.sender_audio.SenderAudioEvent

sealed class SenderImagesEvent {
    data class OnSelectClick(val id: Long) : SenderImagesEvent()

    data object OnSelectAll : SenderImagesEvent()
    data object OnClearAllSelection : SenderImagesEvent()
}