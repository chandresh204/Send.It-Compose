package chad.orionsoft.sendit.ui.qr_connect

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.PermissionDomain
import chad.orionsoft.sendit.domain.QRConnectDomain
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrConnectViewModel @Inject constructor(
    private val qrConnectDomain: QRConnectDomain,
    permissionDomain: PermissionDomain,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _qrState = MutableStateFlow(QrConnectState())
    val qrState = _qrState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateAsPerConnectStatus(qrConnectDomain.getCurrentConnectState())
            ConnectProcessRepository.connectStatusFlow.collectLatest {
                updateAsPerConnectStatus(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            qrConnectDomain.getFailedCauseFlow().collect {
                _qrState.update { state ->
                    state.copy(
                        connectionError = it,
                    )
                }
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isPermission = permissionDomain.isNotificationPermission()
            _qrState.update {
                it.copy(
                    isNotificationPermission = isPermission
                )
            }
        }
    }

    private fun updateAsPerConnectStatus(newStatus: ConnectStatus) {
        _qrState.update { state ->
            state.copy(
                connectStatus = newStatus
            )
        }
        when(newStatus) {
            ConnectStatus.NoNetwork -> {
                _qrState.update { state ->
                    state.copy(
                        qrBitmap = null,
                    )
                }
            }
            ConnectStatus.OnHotspot -> {
                _qrState.update { state ->
                    state.copy(
                        qrBitmap = null,
                    )
                }
            }
            ConnectStatus.OnWifi -> {
                val qrString = qrConnectDomain.generateQRString()
                println("wifi address >>> $qrString")
                val bitmap = qrConnectDomain.getQRBitmap(qrString,
                    RuntimeConstants.screenWidthPx)
                bitmap?.let { bMap ->
                    _qrState.update { state ->
                        state.copy(
                            qrBitmap = bMap,
                        )
                    }
                }
            }
            ConnectStatus.ConnectingDevice -> {
                val connectingDeviceDetail = qrConnectDomain.getPairingDeviceDetails()
                _qrState.update { state ->
                    state.copy(
                        pairingDeviceDetails = connectingDeviceDetail
                    )
                }
            }
            ConnectStatus.DeviceConnected -> {
                val connectingDeviceDetail = qrConnectDomain.getPairingDeviceDetails()
                _qrState.update { state ->
                    state.copy(
                        pairingDeviceDetails = connectingDeviceDetail
                    )
                }
            }
        }
    }

    fun dismissErrorDialog() {
        _qrState.update {
            it.copy(
                connectionError = null
            )
        }
    }

    fun processScannedQR(qrContent: String) {
        if(!qrContent.contains(Constants.APP_CODE)) {
            _qrState.update { state ->
                state.copy(
                    connectionError = Pair(
                        context.getString(R.string.not_send_it_qr_title),
                        context.getString(R.string.not_send_it_qr_desc)
                    )
                )
            }
            return
        }
        _qrState.update { state ->
            state.copy(
                qrScanResult = qrContent
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            qrConnectDomain.processScannedQR(qrContent)
        }
    }

    fun onNotificationPermissionChanged(granted: Boolean) {
        _qrState.update {
            it.copy(
                isNotificationPermission = granted
            )
        }
    }

    fun performNetworkGoneOperations() {
        qrConnectDomain.stopAllUdpProcess()
    }
}