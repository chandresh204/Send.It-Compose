package chad.orionsoft.sendit.ui.sender_audio

sealed class SenderAudioEvent {
    data class OnSelectClick(val id: Long): SenderAudioEvent()
    data class OnSearch(val query: String?) : SenderAudioEvent()
    data object OnSelectAll : SenderAudioEvent()
    data object OnClearAllSelection : SenderAudioEvent()

    data object OnSortingChangeClicked : SenderAudioEvent()
}