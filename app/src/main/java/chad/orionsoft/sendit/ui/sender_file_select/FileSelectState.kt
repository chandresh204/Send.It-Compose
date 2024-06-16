package chad.orionsoft.sendit.ui.sender_file_select

data class FileSelectState(
    val selectFiles: List<FileSelectUiData> = emptyList()
)

data class FileSelectUiData(
    val id: Long,
    val title: String
)