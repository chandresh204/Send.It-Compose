package chad.orionsoft.sendit.ui.sender_file_select

import chad.orionsoft.sendit.model.MediaData

data class FileSelectState(
    val selectFiles: List<MediaData> = emptyList()
)