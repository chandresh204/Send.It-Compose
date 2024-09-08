package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_sending

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.ui.components.InProgressItemCard
import chad.orionsoft.sendit.ui.components.SteadyItemCard
import chad.orionsoft.sendit.utils.Constants

@Composable
fun TransferSendingScreen() {

    val vModel = hiltViewModel<TransferSendingViewModel>()
    val uiState by vModel.transferSendingState.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(Constants.PAGE_PADDING.dp)) {
        LazyColumn(Modifier.animateContentSize()) {
            items(uiState.sentList, key = {
                it.transferringMediaItem.id
            }) {
                Box(modifier = Modifier.animateContentSize()) {
                    if (it.status == ProgressStatus.Sending) {
                        InProgressItemCard(
                            placeHolder = it.transferringMediaItem.thumbnailPlaceHolder,
                            title = it.transferringMediaItem.title,
                            subTitle = it.progressText,
                            thumbnail = it.transferringMediaItem.thumbnailUri,
                            progressInFloat = it.progress
                        )
                    } else {
                        SteadyItemCard(
                            it.transferringMediaItem.thumbnailPlaceHolder,
                            it.transferringMediaItem.title,
                            it.sizeReadable,
                            it.transferringMediaItem.thumbnailUri,
                            it.status
                        )
                    }
                }
            }
        }
    }

}