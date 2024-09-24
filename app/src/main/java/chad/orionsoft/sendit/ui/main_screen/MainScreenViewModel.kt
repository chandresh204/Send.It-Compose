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

    companion object {
        private val _clearAllSelectionFlow = MutableSharedFlow<Boolean>()
        val clearAllSelectionReceiver = _clearAllSelectionFlow.asSharedFlow()
    }
}