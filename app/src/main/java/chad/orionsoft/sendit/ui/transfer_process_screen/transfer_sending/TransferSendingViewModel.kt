package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_sending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.TransferringMediaItem
import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferItemUi
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferSendingViewModel @Inject constructor(): ViewModel() {

    private val _transferSendingState = MutableStateFlow(TransferSendingState())
    val transferSendingState = _transferSendingState.asStateFlow()

    private val tag = "TransferSendingViewModel"

    init {
        updateSendingList(
            TransferProcessDomain.getTheCurrentTransferSendingList(),
            TransferProcessDomain.getSentHistoryList()
        )
        receiveSendingProgressUpdate()
        receiveSendingStatusChanges()
    }

    private fun receiveSendingProgressUpdate() {
        viewModelScope.launch {
            TransferProcessDomain.sendingItemProgressFlow.collectLatest { progressData ->
                val itemToUpdate = _transferSendingState.value.sentList.find {
                    it.transferringMediaItem.id == progressData.id
                }
                itemToUpdate?.let {
                    val index = _transferSendingState.value.sentList.indexOf(it)
                    val newProgressText = Converters.formatDataString(progressData.sent) + "/${it.sizeReadable}"
                    val newProgress = (progressData.sent.toFloat()/it.transferringMediaItem.size)
                    val newSentList = _transferSendingState.value.sentList.toMutableList()
                    newSentList[index] = newSentList[index].copy(
                        progress = newProgress,
                        progressText = newProgressText
                    )
                    _transferSendingState.update { state ->
                        state.copy(
                            sentList = newSentList
                        )
                    }
                }
                /*val inProgressItem = if(_transferSendingState.value.inProgressItem == null) {
                    moveItemFromQueueToInProgress(progressData.id)
                } else {
                    if(_transferSendingState.value.inProgressItem?.transferringMediaItem?.id == progressData.id) {
                        _transferSendingState.value.inProgressItem
                    } else {
                        moveItemFromQueueToInProgress(progressData.id)
                    }
                }
                inProgressItem?.let {
                    val newProgress = progressData.sent.toFloat()/inProgressItem.transferringMediaItem.size
                    val progressText = "${ Converters.formatDataString(progressData.sent) } / ${inProgressItem.transferringMediaItem.sizeReadable}"
                    val updatedItem = it.copy(
                        progress = newProgress,
                        progressText = progressText
                    )
                    _transferSendingState.update { state ->
                        state.copy(
                            inProgressItem = updatedItem
                        )
                    }
                }*/
            }
        }
    }

    private fun receiveSendingStatusChanges() {
//        updateSendingList(TransferProcessDomain.getTheCurrentTransferSendingList())
        viewModelScope.launch {
            TransferProcessDomain.sendingItemStatusFlow.collect { newStatus ->
                val itemToUpdate = _transferSendingState.value.sentList.find {
                    it.transferringMediaItem.id == newStatus.first
                }
                itemToUpdate?.let {
                    val itemList = _transferSendingState.value.sentList.toMutableList()
                    val index = itemList.indexOf(it)
                    itemList[index] = itemList[index].copy(
                        status = newStatus.second
                    )
                    _transferSendingState.update { state ->
                        state.copy(
                            sentList = itemList
                        )
                    }
                }
            }
        }
        /*viewModelScope.launch {
            TransferProcessDomain.sendingItemStatusFlow.collect { newStatus ->
                val itemList = _transferSendingState.value.sentList.toMutableList()
                val iToUpdate = itemList.find { it.id == newStatus.first }
                iToUpdate?.let {
                    val iIndex = itemList.indexOf(iToUpdate)
                    itemList[iIndex] = itemList[iIndex].copy(
                        status = newStatus.second
                    )
                    _transferSendingState.update { state ->
                        state.copy(
                            sentList = itemList,
                            inProgressItem = null
                        )
                    }
                }
            }
        }*/
    }

    /*private fun moveItemFromQueueToInProgress(id: Long) : TransferItemUi? {
        val sendItemMain = _transferSendingState.value.sentList.find {
            it.transferringMediaItem.id == id
        }
        if(sendItemMain == null) {
            Log.e(tag, "moving item in progress, id: $id not found in list. all Ids: ${_transferSendingState.value.sentList.joinToString("\n")}")
            return null
        }
        return TransferItemUi(
            sendItemMain,

        )
    }*/

    private fun updateSendingList(
        queue : List<TransferringMediaItem>,
        history: List<TransferringMediaItem>
    ) {
        val list = mutableListOf<TransferringMediaItem>()
        list.addAll(queue)
        list.addAll(history)
        _transferSendingState.update {
            it.copy(
                sentList = list.map { mItem ->
                    TransferItemUi(
                        mItem,
                        "",
                        Converters.formatDataString(mItem.size),
                        mItem.status,
                        0f,
                    )
                }
            )
        }
    }
}