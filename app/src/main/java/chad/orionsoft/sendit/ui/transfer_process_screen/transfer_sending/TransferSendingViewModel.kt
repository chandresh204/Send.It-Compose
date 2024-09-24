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
        receiveSessionCleared()
    }

    fun removeItemFromQueue(id: Long) {
        val isRemoved = TransferProcessDomain.removeItemFromQueue(id)
        if(isRemoved) {
            // remove from UI List too
            val removeItem = _transferSendingState.value.sentList.find { it.transferringMediaItem.id == id }
            removeItem?.let {
                val newList = _transferSendingState.value.sentList.toMutableList()
                newList.remove(it)
                _transferSendingState.update { state ->
                    state.copy(
                        sentList = newList
                    )
                }
            }
        }
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
            }
        }
    }

    private fun receiveSendingStatusChanges() {
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
    }

    private fun receiveSessionCleared() {
        viewModelScope.launch {
            TransferProcessDomain.clearEverythingFlow.collect {
                _transferSendingState.update {
                    it.copy(
                        sentList = emptyList()
                    )
                }
            }
        }
    }

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