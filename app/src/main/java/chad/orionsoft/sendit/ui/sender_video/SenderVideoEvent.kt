package chad.orionsoft.sendit.ui.sender_video

sealed class SenderVideoEvent {

    data class OnLastVisibleIndexChanged(val lastIndex: Int) : SenderVideoEvent()

    data class OnSelectClick(val id: Long): SenderVideoEvent()

    data object OnSelectAll : SenderVideoEvent()
    data object OnClearAllSelection : SenderVideoEvent()

    data object OnSortingChangeClicked : SenderVideoEvent()
}