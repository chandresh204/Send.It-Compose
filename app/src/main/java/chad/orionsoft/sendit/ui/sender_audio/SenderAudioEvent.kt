package chad.orionsoft.sendit.ui.sender_audio

import chad.orionsoft.sendit.ui.model.SortOrder

sealed class SenderAudioEvent {
    data class OnSelectClick(val id: Long): SenderAudioEvent()
    data class OnSearch(val query: String?) : SenderAudioEvent()
    data object OnSelectAll : SenderAudioEvent()
    data object OnClearAllSelection : SenderAudioEvent()

    data class OnSortingChanged(val newSort: SortOrder) : SenderAudioEvent()
}