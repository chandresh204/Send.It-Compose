package chad.orionsoft.sendit.ui.main_screen

import chad.orionsoft.sendit.model.MediaData

sealed class MainScreenEvent {
    data class OnItemSelected(val mData: MediaData, val isSelected: Boolean) : MainScreenEvent()
}