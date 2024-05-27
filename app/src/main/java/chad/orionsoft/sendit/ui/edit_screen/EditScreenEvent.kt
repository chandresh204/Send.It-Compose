package chad.orionsoft.sendit.ui.edit_screen

sealed class EditScreenEvent {
    data object OnSaveClicked : EditScreenEvent()
    data class OnUseNameUpdated(val new: String) : EditScreenEvent()
}