package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.FileIOOperations
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.model.UDPResponse
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCReceiverEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCReceiverState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.CONNECT_NOW
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.getResponse
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.sendData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CCReceiverViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    private val _ccReceiverState = MutableStateFlow(CCReceiverState())
    val receiverState = _ccReceiverState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _onFinishChannel = Channel<Boolean>()
    val finishReceiver = _onFinishChannel.receiveAsFlow()

    private var currentJob: Job? = null

    private var operationalPort = Constants.DEFAULT_BROADCAST_PORT

    private val tag = "CCReceiverViewModel"

    init {
        ioScope.launch {
            networkObserver.wlanChangeReceiver.collect { connected ->
                if (!connected) {
                    currentJob?.cancel()
                    _ccReceiverState.update {
                        it.copy(
                            currentStep = ReceiverConnectStep.CheckIsWLAN
                        )
                    }
                } else {
                    currentJob?.cancel()
                    startReplyingSender()
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

            }
        }
    }

    private fun startReplyingSender() {
        ioScope.launch {
            val res = replySearch()
            if(res.success) {
                _ccReceiverState.update { state ->
                    state.copy(
                        senderData = ReceiverData(res.partnerAddress, extractUserNameFromResponse(res.response)),
                        currentStep = ReceiverConnectStep.FinalHandshake
                    )
                }
                val connectSig = receiveConnectSignal()
                if(connectSig.success) {
                    if(connectSig.response.contains(CONNECT_NOW)) {
                        _ccReceiverState.update { state ->
                            state.copy(
                                currentStep = ReceiverConnectStep.Connected
                            )
                        }
                    }
                }
            } else {
                _ccReceiverState.update { state ->
                    state.copy(
                        currentStep = ReceiverConnectStep.Error
                    )
                }
            }
        }
    }

    private suspend fun replySearch(): UDPResponse {
        return withContext(Dispatchers.IO) {
            val recSocket = DatagramSocket(Constants.DEFAULT_BROADCAST_PORT)
            var returnResponse : UDPResponse
            while (true) {
                recSocket.soTimeout = 30000
                Log.d("CreateConnection", "r1 -> receiving broadcast")
                val udpResponse = recSocket.getResponse()
                returnResponse = udpResponse
                if (udpResponse.success) {
                    val got = udpResponse.response
                    if (got.contains(Constants.BROADCAST_MSG)) {
                        val presentReply =
                            "${Constants.BROADCAST_MSG}/*${createMyUsername()}*/"
                        Log.d(tag, "Sending broadcast reply to :${udpResponse.partnerAddress}")
                        recSocket.sendData(
                            presentReply,
                            udpResponse.partnerAddress,
                            udpResponse.partnerPort
                        )
                        Log.d("CreateConnection", "r2 -> sent reply to broadcast")
                    }
                    if (got.contains(Constants.RECEIVER_SELECT_MESSAGE)) {
                        Log.d(
                            tag,
                            "Broadcast - Next Signal received :${udpResponse.partnerAddress}"
                        )
                        operationalPort = Random.nextInt(1000, 9999)
                        val connectOkSignal = Constants.RECEIVER_SELECT_MESSAGE +
                                "/*123456*/$operationalPort#6" // TODO add right params
                        Log.d(tag, "Sending connect reply : $connectOkSignal")
                        Log.d("CreateConnection", "r3 -> sent device info")
                        recSocket.sendData(
                            connectOkSignal,
                            udpResponse.partnerAddress,
                            udpResponse.partnerPort,
                            waitForResponse = false
                        )
                        recSocket.close()
                        break
                    }
                } else {
                    break
                }
            }
            return@withContext returnResponse
        }
    }

    private suspend fun receiveConnectSignal() : UDPResponse {
        return withContext(Dispatchers.IO) {
            val recSocket = DatagramSocket(operationalPort)
            recSocket.soTimeout = 15000
            Log.d("CreateConnection", "r4 -> start receiving on new port")
            var returnResponse : UDPResponse
            while (true) {
                val udpResponse = recSocket.getResponse()
                returnResponse = udpResponse
                if(udpResponse.success) {
                    val got = udpResponse.response
                    if(got.contains(CONNECT_NOW)) {
                        val reply = "$CONNECT_NOW/*${FileIOOperations.getMyFreeSpace()}*/"
                        recSocket.sendData(reply, udpResponse.partnerAddress, udpResponse.partnerPort, false)
                        recSocket.close()
                        Log.d("CreateConnection", "r5_a1 -> sent connect ok with free space (FINISH)")
                        break
                    }
                    if(got.contains(Constants.ENTER_CODE_SIGNAL)) {
                        Log.d("CreateConnection", "r5_a1_1 -> Need to enter code")
                        recSocket.close()
                        break
                    }
                    if(got.contains(Constants.CODE_ENTERED_SIGNAL_RESULT)) {
                        Log.d("CreateConnection", "r5_a3_3 -> Received code ans : $got")
                        recSocket.close()
                        break
                    }
                } else {
                    break
                }
            }
            return@withContext returnResponse
        }
    }

    suspend fun sendEnteredCodeToSender(code: String, toAddress: InetAddress, toPort: Int) {
        withContext(Dispatchers.IO) {
            val dSocket = DatagramSocket()
            val codeSig = "${Constants.ENTER_CODE_SIGNAL}/*$code*/"
            dSocket.sendData(codeSig, toAddress, toPort, waitForResponse = false)
            Log.d("CreateConnection", "r5_a2 -> Sent connect code to sender")
        }
    }

    private fun createMyUsername(): String {
        val uName = preferenceRepository.getUserNameFromPreferences()
        return "$uName - ${Build.BRAND}"
    }

    private fun extractUserNameFromResponse(res: String) : String {
        val indexOfStart = res.indexOf("/*")
        val indexOfEnd = res.indexOf("*/")
        return res.substring(indexOfStart +2, indexOfEnd)
    }
}