package chad.orionsoft.sendit.ui.sender_video

import chad.orionsoft.sendit.ui.model.SortOrder

sealed class SenderVideoEvent {

    data class OnSelectClick(val id: Long): SenderVideoEvent()

    data class OnSearch(val query: String?) : SenderVideoEvent()

    data object OnSelectAll : SenderVideoEvent()
    data object OnClearAllSelection : SenderVideoEvent()

    data class OnSortingChanged(val sortOrder: SortOrder) : SenderVideoEvent()
}