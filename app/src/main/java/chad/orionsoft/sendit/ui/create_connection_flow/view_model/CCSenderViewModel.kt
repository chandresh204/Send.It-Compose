package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.domain.UDPOperations
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCSenderEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCSenderState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ConnectStep
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CCSenderViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val udpOperations: UDPOperations
) : ViewModel() {

    private val _ccSenderState = MutableStateFlow(CCSenderState())
    val ccSenderState = _ccSenderState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var isOnWlan = false

    private val tag = "CCSenderViewModel"

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
                }
            }
        }
    }

    fun onEvent(event: CCSenderEvent) {
        when(event) {
            CCSenderEvent.OnStartConnectionProcess -> {
                val nextState = if(isOnWlan) {
                    startSearchingReceiver()
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
                currentJob?.cancel("user intterupt")
                ioScope.launch {
                    _onFinishChannel.send(true)
                }
            }

            is CCSenderEvent.OnReceiverSelected -> {
                val isKnownReceiver = false // TODO get details from shared prefs
                _ccSenderState.update {
                    it.copy(
                        currentStep = if (!isKnownReceiver) ConnectStep.ShowConnectCode
                        else ConnectStep.FinalHandshake,
                        selectedReceiver = event.name
                    )
                }


                ioScope.launch {
                    delay(5000)
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.FinalHandshake,
                        )
                    }
                    delay(5000)
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.Connected,
                        )
                    }
                }
            }
        }
    }

    private fun startSearchingReceiver() {
        currentJob = viewModelScope.launch {
            val broadCastAddress = udpOperations.obtainBroadcastAddress()
            if(broadCastAddress == null) {
                Log.e(tag, "failed to obtain broadcast address")
                onEvent(CCSenderEvent.OnCancelProcess)
                return@launch
            }
            udpOperations.broadcastSearch(broadCastAddress)
            // TODO continue process
        }
    }

}