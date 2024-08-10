package chad.orionsoft.sendit.ui.main_screen

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.repository.ConnectionRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.work_manager.TcpSenderWorker
import chad.orionsoft.sendit.work_manager.TcpServerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainScreenState = _mainScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            ConnectionRepository.connectionUpdateReceiver.collectLatest {
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
    }

    fun onEvent(event: MainScreenEvent) {
        when(event) {
            is MainScreenEvent.OnItemSelected -> {
                _mainScreenState.update {
                    it.copy(
                        selectionModel = updateSelectionUiModel(event.mData, event.isSelected)
                    )
                }
            }

            MainScreenEvent.OnItemSendClick -> {
                if(mainScreenState.value.connectedDeviceDetails == null) {
                    Toast.makeText(context, "Not connected", Toast.LENGTH_LONG).show()
                } else {
                    startTCPSender(mainScreenState.value.connectedDeviceDetails!!.serverPort)
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
        println("Trying to start server on $serverPort")
        val wM = WorkManager.getInstance(context)
        wM.cancelAllWorkByTag(Constants.SERVER_WM_TAG)
        val workRequest = OneTimeWorkRequestBuilder<TcpServerWorker>()
            .setInputData(Data.Builder().apply {
                putInt(Constants.TCP_SERVER_WM_PORT, serverPort)
                putString(Constants.TCP_SERVER_WM_DEVICE, deviceName)
            }.build())
            .addTag(Constants.SERVER_WM_TAG).build()
        wM.enqueue(workRequest)
    }

    private fun startTCPSender(serverPort: Int) {
        println("Trying to start sender")
        val wM = WorkManager.getInstance(context)
        wM.cancelAllWorkByTag(Constants.SENDER_WM_TAG)
        val workRequest = OneTimeWorkRequestBuilder<TcpSenderWorker>()
            .setInputData(Data.Builder().apply {
                putInt(Constants.TCP_SERVER_WM_PORT, serverPort)
            }.build())
            .addTag(Constants.SENDER_WM_TAG).build()
        wM.enqueue(workRequest)
    }
}