package chad.orionsoft.sendit.ui.sender_audio

sealed class SenderAudioEvent {
    data class OnLastVisibleIndexChanged(val lastIndex: Int) : SenderAudioEvent()
    data class OnSelectClick(val id: Long): SenderAudioEvent()
}