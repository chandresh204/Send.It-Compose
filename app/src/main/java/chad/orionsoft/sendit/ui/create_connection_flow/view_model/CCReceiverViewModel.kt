package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCReceiverEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCReceiverState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverConnectStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CCReceiverViewModel @Inject constructor(
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val _ccReceiverState = MutableStateFlow(CCReceiverState())
    val receiverState = _ccReceiverState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _onFinishChannel = Channel<Boolean>()
    val finishReceiver = _onFinishChannel.receiveAsFlow()

    init {
        ioScope.launch {
            networkObserver.wlanChangeReceiver.collect { connected ->
                if(!connected) {
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.CheckIsWLAN
                        )
                    }
                } else {
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.Waiting
                        )
                    }
                    delay(5000)
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.Waiting
                        )
                    }
                    val isUnknownSender = true
                    if(isUnknownSender) {
                        _ccReceiverState.update {
                            it.copy(
                                currentStep = ReceiverConnectStep.EnterCode,
                                senderName = "Dummy Sender"
                            )
                        }
                    } else {
                        dummyConnectProcess()
                    }

                }
            }
        }
    }

    fun onEvent(event: CCReceiverEvent) {
        when(event) {
            is CCReceiverEvent.OnCodeEntered -> {
                ioScope.launch {
                    dummyConnectProcess()
                }
            }
        }
    }

    private suspend fun dummyConnectProcess() {
        _ccReceiverState.update {
            it.copy(
                currentStep = ReceiverConnectStep.FinalHandshake,
                senderName = "Dummy Sender"
            )
        }
        delay(5000)
        _ccReceiverState.update {
            it.copy(
                currentStep = ReceiverConnectStep.Connected
            )
        }
        delay(5000)
        _onFinishChannel.send(true)
    }
}