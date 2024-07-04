package chad.orionsoft.sendit.ui.receiver_main

import androidx.annotation.DrawableRes

data class ReceiverMainState(
    val isConnected: Boolean = false,
    val receivedList : List<FileUIData> = emptyList(),
    val inProgress: FileUIData? = null
)

data class FileUIData(
    val title: String,
    @DrawableRes val icon: Int,
    val infoString: String,
    val progressState: Float
)