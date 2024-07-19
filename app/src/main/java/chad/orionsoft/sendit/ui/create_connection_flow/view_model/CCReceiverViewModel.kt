package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.domain.ReceiverConnectionProcess
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.repository.ConnectionRepository
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCReceiverEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCReceiverState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.CODE_ENTERED_SIGNAL_RESULT
import chad.orionsoft.sendit.utils.Constants.CONNECT_NOW
import chad.orionsoft.sendit.utils.Constants.ENTER_CODE_SIGNAL
import chad.orionsoft.sendit.utils.Constants.RECEIVER_SELECT_MESSAGE
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CCReceiverViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val connectionRepository: ConnectionRepository,
    private val connectionProcess: ReceiverConnectionProcess,
) : ViewModel() {

    private val _ccReceiverState = MutableStateFlow(CCReceiverState())
    val receiverState = _ccReceiverState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _onFinishChannel = Channel<Boolean>()
    val finishReceiver = _onFinishChannel.receiveAsFlow()

    private var operationalPort = Constants.DEFAULT_BROADCAST_PORT

    var senderDetails : ReceiverData? = null

    private val tag = "CCReceiverViewModel"

    init {
        ioScope.launch {
            networkObserver.wlanChangeReceiver.collect { connected ->
                if (!connected) {
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.CheckIsWLAN
                        )
                    }
                } else {
                    connectionProcess.initProcess().collect {
                        Log.d(tag, "onFlow: $it")
                        if(it.success) {
                            when {
                                it.response.contains(RECEIVER_SELECT_MESSAGE) -> {
                                    val tripleData = Converters.extractTripleData(it.response) // check Constant file for pattern info
                                    senderDetails = ReceiverData(
                                        it.partnerAddress, tripleData.third,
                                        operationalPort,
                                        tripleData.first,
                                        Converters.convertStringToColor(tripleData.second)
                                    )
                                    _ccReceiverState.update { state ->
                                        state.copy(
                                            currentStep = ReceiverConnectStep.FinalHandshake
                                        )
                                    }
                                }
                                it.response.contains(ENTER_CODE_SIGNAL) -> {
                                    _ccReceiverState.update { state ->
                                        state.copy(
                                            currentStep = ReceiverConnectStep.EnterCode
                                        )
                                    }
                                    ioScope.launch {
                                        connectionProcess.enterCodeTimerReceiver.collect {
                                            _ccReceiverState.update { state ->
                                                state.copy(
                                                    enterCodeTimer = it
                                                )
                                            }
                                        }
                                    }
                                }
                                it.response.contains(CODE_ENTERED_SIGNAL_RESULT) -> {
                                    val result = Converters.extractDataInNormalCase(it.response)
                                    if(result == "true") {
                                        _ccReceiverState.update { state ->
                                            state.copy(
                                                currentStep = ReceiverConnectStep.Connected
                                            )
                                        }
                                        setAsConnectedDevice()
                                    } else {
                                        setError("You have entered wrong code!!")
                                    }
                                }
                                it.response.contains(CONNECT_NOW) -> {
                                    _ccReceiverState.update { state ->
                                        state.copy(
                                            currentStep = ReceiverConnectStep.Connected
                                        )
                                    }
                                    setAsConnectedDevice()
                                }
                            }
                        } else {
                            setError(it.response)
                        }
                    }
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.Waiting
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: CCReceiverEvent) {
        when (event) {
            is CCReceiverEvent.OnCodeEntered -> {
                _ccReceiverState.update {
                    it.copy(
                        currentStep = ReceiverConnectStep.FinalHandshake
                    )
                }
                ioScope.launch {
                    connectionProcess.connectCode = event.code
                }
            }

            is CCReceiverEvent.OnCancelSearch -> {
                ioScope.launch {
                    _onFinishChannel.send(connectionProcess.cancelProcess(true))
                }
            }
        }
    }

    private fun setError(msg: String? = null) {
        _ccReceiverState.update {
            it.copy(
                currentStep = ReceiverConnectStep.Error,
                error = msg
            )
        }
        ioScope.launch {
            _onFinishChannel.send(connectionProcess.cancelProcess(false))
        }
    }

    private fun setAsConnectedDevice() {
        if(senderDetails == null) {
            Log.e(tag, "Error setting connected device, sender details null")
            return
        }
        connectionRepository.updateConnectionInfo(
            ConnectedDeviceDetails(
                senderDetails!!.name, senderDetails!!.address, senderDetails!!.deviceType,
                senderDetails!!.color,
                -1
            )
        )
        /*RuntimeConstants.connectedDeviceDetails = ConnectedDeviceDetails(
            senderDetails!!.name, senderDetails!!.address, senderDetails!!.deviceType,
            senderDetails!!.color,
            -1
        )*/
    }
}