package chad.orionsoft.sendit.ui.sender_file_select

import android.content.Intent
import chad.orionsoft.sendit.model.MediaData

sealed class FileSelectEvent {
    data class OnFileData(val intent: Intent, val addIfDataFound: (MediaData) -> Unit) : FileSelectEvent()
    data class OnFileRemove(val id: Long) : FileSelectEvent()
}