package chad.orionsoft.sendit.model

import android.net.Uri

data class TransferringMediaItem(
    val id: Long,
    val title: String,
    val uri: Uri?, //  not required for receiving files
    val thumbnailUri: Uri?,
    val thumbnailPlaceHolder: Int,
    val size: Long,
    val mediaType: FileType,
    val status: ProgressStatus
)