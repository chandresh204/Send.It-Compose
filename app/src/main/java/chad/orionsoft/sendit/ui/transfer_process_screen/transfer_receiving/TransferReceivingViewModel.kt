package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving

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
        receiveProgressData()
        receiveStatusChange()
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

    private fun receiveCurrentList() {
        viewModelScope.launch {
            TransferProcessDomain.transferReceivingListFlow.collect { newItem ->
                val newList = _transferReceivingState.value.receiveList.toMutableList()
                if(newList.find {
                    it.transferringMediaItem.id == newItem.id
                    } == null) {
                    newList.add(0,
                        TransferItemUi(
                            TransferringMediaItem(
                                newItem.id,
                                newItem.title,
                                newItem.uri,
                                null,
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
                    )
                    updateList(newList)
                }
            }
        }
    }

    private fun updateList(newList: List<TransferItemUi>) {
        _transferReceivingState.update {
            it.copy(
                receiveList = newList
            )
        }
    }
}