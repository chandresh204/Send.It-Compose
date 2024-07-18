package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.domain.SenderConnectionProcess
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCSenderEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCSenderState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.CONNECT_NOW
import chad.orionsoft.sendit.utils.Constants.ENTER_CODE_SIGNAL
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class CCSenderViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val connectionProcess: SenderConnectionProcess,
) : ViewModel() {

    private val _ccSenderState = MutableStateFlow(CCSenderState())
    val ccSenderState = _ccSenderState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var isOnWlan = false

    var receiverDetails : ReceiverData? = null

    private val _onFinishChannel = Channel<Boolean>()
    val finishReceiver = _onFinishChannel.receiveAsFlow()

    private var currentJob : Job? = null

    init {
        ioScope.launch {
            networkObserver.wlanChangeReceiver.collect { isOnWLAN ->
                isOnWlan = isOnWLAN
                if(isOnWlan && ccSenderState.value.currentStep == ConnectStep.CheckIsWLAN) {
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.SearchReceiver
                        )
                    }
                    receiverDetails = null
                    startConnectionProcess()
                }
            }
        }
    }

    private fun startConnectionProcess() {
       ioScope.launch {
           currentJob?.cancelAndJoin()
           currentJob = ioScope.launch {
               connectionProcess.initProcess().collect { udpResult ->
                   if(udpResult.success) {
                       when {
                           udpResult.response.contains(Constants.BROADCAST_MSG) -> {
                               extractReceiverDataAndAddToList(udpResult.response, udpResult.partnerAddress)
                           }
                           udpResult.response.contains(CONNECT_NOW) -> {
                               _ccSenderState.update {
                                   it.copy(
                                       currentStep = ConnectStep.Connected
                                   )
                               }
                           }
                           udpResult.response.contains(ENTER_CODE_SIGNAL) -> {
                               ioScope.launch {
                                   connectionProcess.enterCodeTimerReceiver.collect {
                                       _ccSenderState.update { state ->
                                           state.copy(
                                               enterCodeTimer = it
                                           )
                                       }
                                   }
                               }
                               _ccSenderState.update {
                                   it.copy(
                                       currentStep = ConnectStep.ShowConnectCode,
                                       connectCode = Converters.extractDataInNormalCase(udpResult.response)
                                   )
                               }
                           }
                       }
                   } else {
                       setError(udpResult.response)
                   }
               }
           }
       }
    }

    fun onEvent(event: CCSenderEvent) {
        when(event) {
            CCSenderEvent.OnStartConnectionProcess -> {
                val nextState = if(isOnWlan) {
                    receiverDetails = null
                    startConnectionProcess()
                    ConnectStep.SearchReceiver
                } else {
                    ConnectStep.CheckIsWLAN
                }
                _ccSenderState.update {
                    it.copy(
                        currentStep = nextState
                    )
                }
            }

            CCSenderEvent.OnCancelProcess -> {
                receiverDetails = null
                ioScope.launch {
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.FinalHandshake
                        )
                    }
                    currentJob?.cancelAndJoin()
                    _onFinishChannel.send(true)
                }

            }

            is CCSenderEvent.OnReceiverSelected -> {
                receiverDetails = ccSenderState.value.receiverList.find {
                    it.name == event.receiverData.name
                }
                if(receiverDetails == null) {
                    setError("Receiver details not found")
                } else {
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.FinalHandshake,
                        )
                    }
                    connectionProcess.selectReceiver(receiverDetails!!)
                }
            }
        }
    }

    private fun extractReceiverDataAndAddToList(msg: String, address: InetAddress) {
        val infoTriple = Converters.extractTripleData(msg)
        val receiverData = ReceiverData(
            address,
            name = infoTriple.third,
            deviceType = infoTriple.first,
            color = Converters.convertStringToColor(infoTriple.second),
        )
        val oldList = ccSenderState.value.receiverList.toMutableList()
        val addToList = {
            oldList.add(receiverData)
            _ccSenderState.update {
                it.copy(
                    receiverList = oldList,
//                    receiverListToDisplay = oldList.map { rData ->
//                        rData.name
//                    }
                )
            }
        }
        if(oldList.isEmpty()) {
            addToList()
            return
        }
        val already = oldList.find { it.name == receiverData.name }
        if(already == null) {
            addToList()
        }
    }

    private fun setError(msg: String? = null) {
        _ccSenderState.update {
            it.copy(
                currentStep = ConnectStep.Error,
                error = msg
            )
        }
    }

}