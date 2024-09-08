package chad.orionsoft.sendit.model

import android.net.Uri

data class TransferringMediaItem(
    val id: Long,
    val title: String,
    val thumbnailUri: Uri?,
    val thumbnailPlaceHolder: Int,
    val size: Long,
    val status: ProgressStatus
)