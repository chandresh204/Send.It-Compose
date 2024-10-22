package chad.orionsoft.sendit.ui.main_screen

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import chad.orionsoft.sendit.domain.QRConnectDomain
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.work_manager.TcpSenderWorker
import chad.orionsoft.sendit.work_manager.TcpServerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val qrConnectDomain: QRConnectDomain
) : ViewModel() {

    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainScreenState = _mainScreenState.asStateFlow()

    private val selectedItemsToSend : MutableList<MediaData> = mutableListOf()

    init {
        viewModelScope.launch {
            ConnectProcessRepository.connectionUpdateReceiver.collectLatest {
                _mainScreenState.update { state ->
                    state.copy(
                        connectedDeviceDetails = it
                    )
                }
                if(it != null) {
                    startTCPServer(it.serverPort, it.name)
                } else {
                    stopAllTCPWork()
                }
            }
        }
        val pairedDevice = qrConnectDomain.getPairingDeviceDetails()
        if(pairedDevice != null) {
            startTCPServer(pairedDevice.serverPort, pairedDevice.name)
            _mainScreenState.update { state ->
                state.copy(
                    connectedDeviceDetails = qrConnectDomain.getPairingDeviceDetails()
                )
            }
        }
        receiveTransferSpeedFlow()
        receiveCurrentSendingCountFlow()
        receiveCurrentReceivingCountFlow()
        receiveCurrentSendingItemTitle()
        receiveCurrentSendingProgress()
        receiveCurrentReceivingProgress()
        receiveCurrentReceivingItemTitle()
    }

    fun onEvent(event: MainScreenEvent) {
        when(event) {
            is MainScreenEvent.OnItemSelected -> {
                updateSelectedItems(event.mData, event.isSelected)
                _mainScreenState.update {
                    it.copy(
                        selectionModel = updateSelectionUiModel(event.mData, event.isSelected),
                    )
                }
            }

            MainScreenEvent.OnItemSendClick -> {
                if(mainScreenState.value.connectedDeviceDetails == null) {
                    Toast.makeText(context, "Not connected", Toast.LENGTH_LONG).show()
                } else {
                    TransferProcessDomain.addToSendingList(selectedItemsToSend)
                    clearAllSelection()
                    if(!TransferProcessDomain.isSending) {
                        startTCPSender(mainScreenState.value.connectedDeviceDetails!!.serverPort)
                    }
                }
            }

            MainScreenEvent.DisconnectNow -> {
                viewModelScope.launch {
                    qrConnectDomain.sendDisconnectSignal()
                }
            }
        }

    }

    private fun updateSelectionUiModel(mediaData: MediaData, addOrRemoved: Boolean) : SelectionUiModel {
        val newCount =if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedCount + 1
        } else {
            _mainScreenState.value.selectionModel.selectedCount - 1
        }
        val newSize = if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedSize + mediaData.size
        } else {
            _mainScreenState.value.selectionModel.selectedSize - mediaData.size
        }
        val displaySize = Converters.formatDataString(newSize)
        return SelectionUiModel(newCount, newSize, displaySize)
    }

    private fun startTCPServer(serverPort: Int, deviceName: String) {
        if(!TcpServerWorker.isTCPServerRunning) {
            println("Trying to start server on $serverPort")
            val wM = WorkManager.getInstance(context)
            wM.cancelAllWorkByTag(Constants.SERVER_WM_TAG)
            val workRequest = OneTimeWorkRequestBuilder<TcpServerWorker>()
                .setInputData(Data.Builder().apply {
                    putInt(Constants.TCP_SERVER_WM_PORT, serverPort)
                    putString(Constants.TCP_SERVER_WM_DEVICE, deviceName)
                }.build())
                .addTag(Constants.SERVER_WM_TAG).build()
            wM.enqueueUniqueWork(Constants.SERVER_WM_TAG, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    private fun startTCPSender(serverPort: Int) {
        println("Trying to start sender")
        val wM = WorkManager.getInstance(context)
        val wInfo = wM.getWorkInfosByTag(Constants.SENDER_WM_TAG)
        wInfo.isDone
        wInfo.isCancelled
        wInfo.get().forEach {
            println("workInfo: $it")
        }
        if(wInfo.isDone || wInfo.isCancelled) {
            wM.cancelAllWorkByTag(Constants.SENDER_WM_TAG)
            val workRequest = OneTimeWorkRequestBuilder<TcpSenderWorker>()
                .setInputData(Data.Builder().apply {
                    putInt(Constants.TCP_SERVER_WM_PORT, serverPort)
                }.build())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1L,
                    TimeUnit.MINUTES
                ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(Constants.SENDER_WM_TAG).build()
            wM.enqueueUniqueWork(Constants.SENDER_WM_TAG, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    private fun stopAllTCPWork() {
        val wM = WorkManager.getInstance(context)
        wM.cancelAllWorkByTag(Constants.SENDER_WM_TAG)
        wM.cancelAllWorkByTag(Constants.SERVER_WM_TAG)
    }

    @Synchronized
    private fun updateSelectedItems(mData: MediaData, isSelected: Boolean) {
        if(isSelected) {
            addToSelected(mData)
        } else {
            removeFromSelected(mData)
        }
    }

    private fun addToSelected(mediaData: MediaData) {
        selectedItemsToSend.add(mediaData)
    }

    private fun removeFromSelected(mediaData: MediaData) {
        selectedItemsToSend.remove(mediaData)
    }

    private fun clearAllSelection() {
        selectedItemsToSend.clear()
        viewModelScope.launch {
            _clearAllSelectionFlow.emit(true)
        }
        _mainScreenState.update {
            it.copy(
                selectionModel = SelectionUiModel()
            )
        }
    }

    private fun receiveTransferSpeedFlow() {
        viewModelScope.launch {
            TransferProcessDomain.transferSpeedFlow.collect {
                // sending card logic
                if(it.first <= 0) {
                    _mainScreenState.update { state ->
                        state.copy(
                            sendingFlowInfo = mainScreenState.value.sendingFlowInfo.copy(
                                isVisible = false
                            )
                        )
                    }
                } else {
                    val currentStatus = mainScreenState.value.sendingFlowInfo
                    val rateAndUnit = Converters.formatRateWithUnit(it.first)
                    val newStatus = currentStatus.copy(
                        isVisible = true,
                        transferRate = rateAndUnit.first,
                        transferRateUnit = rateAndUnit.second,
                    )
                    _mainScreenState.update { state ->
                        state.copy(
                            sendingFlowInfo = newStatus
                        )
                    }
                }
                // receiving card logic
                val receivingStatus = mainScreenState.value.receivingFlowInfo
                val reteAndUnit = Converters.formatRateWithUnit(it.second)
                val newStatus = if (it.second > 0) {
                    receivingStatus.copy(
                        isVisible = true,
                        transferRate = reteAndUnit.first,
                        transferRateUnit = reteAndUnit.second
                    )
                } else {
                    receivingStatus.copy(
                        transferRate = reteAndUnit.first,
                        transferRateUnit = reteAndUnit.second
                    )
                }
                val totalSentAndReceived = Pair(
                    Converters.formatDataString(TransferProcessDomain.getTotalSentInSession()),
                    Converters.formatDataString(TransferProcessDomain.getTotalReceivedInSession())
                )
                _mainScreenState.update { state ->
                    state.copy(
                        receivingFlowInfo = newStatus,
                        totalSentAndReceived = totalSentAndReceived
                    )
                }
            }
        }
    }

    private fun receiveCurrentSendingCountFlow() {
        viewModelScope.launch {
            TransferProcessDomain.sendingItemCountFlow.collectLatest {
                val oldStatus = mainScreenState.value.sendingFlowInfo
                val currentDetailsArray = oldStatus.transferItemDetail
                currentDetailsArray[0] = "(${it.first}/${it.second})"
                val newStatus = oldStatus.copy(
                    transferItemDetail = currentDetailsArray
                )
                _mainScreenState.update { status ->
                    status.copy(
                        sendingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    private fun receiveCurrentReceivingCountFlow() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemCountFlow.collectLatest {
                val oldStatus = mainScreenState.value.receivingFlowInfo
                val currentDetailsArray = oldStatus.transferItemDetail
                currentDetailsArray[0] = "(${it+1})"
                val newStatus = oldStatus.copy(
                    transferItemDetail = currentDetailsArray,
                    transferRate = "0",
                    currentItemProgress = 1f,
                )
                _mainScreenState.update { status ->
                    status.copy(
                        receivingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    private fun receiveCurrentSendingItemTitle() {
        viewModelScope.launch {
            TransferProcessDomain.sendingItemTitleFlow.collectLatest {
                val oldStatus = mainScreenState.value.sendingFlowInfo
                val currentDetailsArray = oldStatus.transferItemDetail
                currentDetailsArray[1] = it
                val newStatus = oldStatus.copy(
                    transferItemDetail = currentDetailsArray
                )
                _mainScreenState.update { status ->
                    status.copy(
                        sendingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    private fun receiveCurrentReceivingItemTitle() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemTitleFlow.collectLatest {
                val oldStatus = mainScreenState.value.receivingFlowInfo
                val currentDetailsArray = oldStatus.transferItemDetail
                currentDetailsArray[1] = it
                val newStatus = oldStatus.copy(
                    transferItemDetail = currentDetailsArray
                )
                _mainScreenState.update { status ->
                    status.copy(
                        receivingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    private fun receiveCurrentSendingProgress() {
        viewModelScope.launch {
            TransferProcessDomain.sendingItemProgressPercFlow.collectLatest {
                val oldStatus = mainScreenState.value.sendingFlowInfo
                val newStatus = oldStatus.copy(
                    currentItemProgress = it
                )
                _mainScreenState.update { status ->
                    status.copy(
                        sendingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    private fun receiveCurrentReceivingProgress() {
        viewModelScope.launch {
            TransferProcessDomain.receivingItemProgressPercFlow.collectLatest {
                val oldStatus = mainScreenState.value.receivingFlowInfo
                val newStatus = oldStatus.copy(
                    currentItemProgress = it
                )
                _mainScreenState.update { state ->
                    state.copy(
                        receivingFlowInfo = newStatus
                    )
                }
            }
        }
    }

    companion object {
        private val _clearAllSelectionFlow = MutableSharedFlow<Boolean>()
        val clearAllSelectionReceiver = _clearAllSelectionFlow.asSharedFlow()
    }
}