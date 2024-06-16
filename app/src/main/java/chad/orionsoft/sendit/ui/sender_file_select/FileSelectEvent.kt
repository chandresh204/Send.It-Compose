package chad.orionsoft.sendit.ui.sender_file_select

sealed class FileSelectEvent {
    data object OnAddNewClicked : FileSelectEvent()
}