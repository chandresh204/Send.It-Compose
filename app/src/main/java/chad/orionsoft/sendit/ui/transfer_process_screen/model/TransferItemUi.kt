package chad.orionsoft.sendit.ui.transfer_process_screen.model

import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.model.TransferringMediaItem

data class TransferItemUi(
    val transferringMediaItem : TransferringMediaItem,
    val progressText: String,
    val sizeReadable: String,
    val status: ProgressStatus,
    val progress : Float // between 0 and 1
)