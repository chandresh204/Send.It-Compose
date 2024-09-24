package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.TransferringMediaItem
import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferItemUi
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferReceivingViewModel : ViewModel() {

    private val _transferReceivingState = MutableStateFlow(TransferReceivingState())
    val transferReceivingState = _transferReceivingState.asStateFlow()

    init {
        receiveCurrentList()
        observeList()
        receiveProgressData()
        receiveStatusChange()
        receiveThumbnailUpdate()
        receiveEverythingCleared()
    }

    fun deleteFile(id: Long, context: Context) {
        if(!TransferProcessDomain.deleteReceivedFile(id, context)) {
            Toast.makeText(context, "Something went wrong while deleting file, file not deleted",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun receiveProgressData() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemProgressData.collectLatest { progress ->
                val iToUpdate = _transferReceivingState.value.receiveList.find {
                    it.transferringMediaItem.id == progress.id
                }
                iToUpdate?.let {
                    val newList = _transferReceivingState.value.receiveList.toMutableList()
                    val index = newList.indexOf(it)
                    newList[index] = newList[index].copy(
                        progressText = "${Converters.formatDataString(progress.received)}/${it.sizeReadable}",
                        progress = progress.received.toFloat()/it.transferringMediaItem.size
                    )
                    updateList(newList)
                }
            }
        }
    }

    private fun receiveStatusChange() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemStatusFlow.collect { statusPair ->
                val iToUpdate = _transferReceivingState.value.receiveList.find {
                    it.transferringMediaItem.id == statusPair.first
                }
                iToUpdate?.let {
                    val newList = _transferReceivingState.value.receiveList.toMutableList()
                    val index = newList.indexOf(it)
                    newList[index] = newList[index].copy(
                        status = statusPair.second
                    )
                    updateList(newList)
                }
            }
        }
    }

    private fun observeList() {
        viewModelScope.launch {
            TransferProcessDomain.transferReceivingListFlow.collect { newItem ->
                val newList = _transferReceivingState.value.receiveList.toMutableList()
                if(newList.find {
                        it.transferringMediaItem.id == newItem.id
                    } == null) {
                    newList.add(0, mapNewToUiModel(newItem))
                    updateList(newList.distinct())
                }
            }
        }
    }

    private fun receiveThumbnailUpdate() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemThumbnailFlow.collect { thumbPair ->
                val newList = _transferReceivingState.value.receiveList.toMutableList()
                val iToUpdate = newList.find { it.transferringMediaItem.id == thumbPair.first }
                iToUpdate?.let {
                    val index = newList.indexOf(it)
                    val newMediaItem = it.transferringMediaItem.copy(
                        thumbnailUri = thumbPair.second
                    )
                    newList[index] = newList[index].copy(
                        transferringMediaItem = newMediaItem
                    )
                    _transferReceivingState.update { state ->
                        state.copy(
                            receiveList = newList
                        )
                    }
                }
            }
        }
    }

    private fun receiveCurrentList() {
        val itemList = TransferProcessDomain.getTheCurrentTransferReceivingList().map {
                mapNewToUiModel(it)
            }
        val newList = _transferReceivingState.value.receiveList.toMutableList()
        newList.addAll(itemList.sortedBy { it.status })
        _transferReceivingState.update {
            it.copy(
                receiveList = newList.distinct()
            )
        }
    }

    private fun receiveEverythingCleared() {
        viewModelScope.launch {
            TransferProcessDomain.clearEverythingFlow.collect {
                _transferReceivingState.update {
                    it.copy(
                        receiveList = emptyList()
                    )
                }
            }
        }
    }

    private fun mapNewToUiModel(newItem: TransferringMediaItem) : TransferItemUi {
        return TransferItemUi(
                TransferringMediaItem(
                    newItem.id,
                    newItem.title,
                    newItem.uri,
                    newItem.thumbnailUri,
                    newItem.thumbnailPlaceHolder,
                    newItem.size,
                    newItem.mediaType,
                    newItem.status,
                ),
                progressText = "",
                sizeReadable = Converters.formatDataString(newItem.size),
                status = newItem.status,
                progress = 0f
            )
    }

    private fun updateList(newList: List<TransferItemUi>) {
        _transferReceivingState.update {
            it.copy(
                receiveList = newList
            )
        }
    }
}