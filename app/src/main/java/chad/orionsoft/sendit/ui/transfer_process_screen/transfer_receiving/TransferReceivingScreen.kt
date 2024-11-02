package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.components.InProgressItemCard
import chad.orionsoft.sendit.ui.components.SteadyItemCard
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.RuntimeConstants

@Composable
fun TransferReceivingScreen(vModel : TransferReceivingViewModel = hiltViewModel()) {
    val uiState by vModel.transferReceivingState.collectAsState()
    val context = LocalContext.current

    var deleteRequestForId by remember {
        mutableLongStateOf(-1L)
    }

    if(deleteRequestForId > 0) {
        BottomSheetWith2Buttons(
            title = stringResource(id = R.string.delete_file),
            positiveButton = {
                TextWithIcon(Icons.Default.Delete, text = stringResource(R.string.delete))
            },
            negativeButton = {
                TextWithIcon(Icons.Default.Close, text = stringResource(R.string.delete_no))
            },
            positiveButtonClick = {
                vModel.deleteFile(deleteRequestForId, context)
                deleteRequestForId = -1
            },
            negativeButtonClick = {
                deleteRequestForId = -1
            }) {
            deleteRequestForId = -1
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(Constants.PAGE_PADDING.dp)) {
        LazyColumn(Modifier.animateContentSize()) {
            items(uiState.receiveList, key = {
                it.transferringMediaItem.id
            }) {
                Box(modifier = Modifier.animateContentSize()) {
                    if (it.status == ProgressStatus.Receiving) {
                        InProgressItemCard(
                            placeHolder = it.transferringMediaItem.thumbnailPlaceHolder,
                            modifier = Modifier.padding(8.dp),
                            title = it.transferringMediaItem.title,
                            subTitle = it.progressText,
                            thumbnail = it.transferringMediaItem.thumbnailUri,
                            color = RuntimeConstants.theirColor,
                            progressInFloat = it.progress
                        )
                    } else {
                        SteadyItemCard(
                            it.transferringMediaItem.thumbnailPlaceHolder,
                            modifier = Modifier.padding(8.dp),
                            it.transferringMediaItem.title,
                            it.sizeReadable,
                            it.transferringMediaItem.thumbnailUri,
                            it.status,
                            disabled = it.status == ProgressStatus.Deleted,
                            removeIcon = {
                                IconButton(onClick = {
                                    deleteRequestForId = it.transferringMediaItem.id
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "delete")
                                }
                            }
                        ) {
                            StartActivity.openFile(context, it.transferringMediaItem.title,
                                it.transferringMediaItem.uri)
                        }
                    }
                }
            }
        }
    }
}