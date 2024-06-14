package chad.orionsoft.sendit.ui.sender_video

sealed class SenderVideoEvent {

    data class OnSelectClick(val id: Long): SenderVideoEvent()

    data class OnSearch(val query: String?) : SenderVideoEvent()

    data object OnSelectAll : SenderVideoEvent()
    data object OnClearAllSelection : SenderVideoEvent()

    data object OnSortingChangeClicked : SenderVideoEvent()
}