package chad.orionsoft.sendit.ui.create_connection_flow.view_model

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.model.UDPResponse
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCSenderEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.CCSenderState
import chad.orionsoft.sendit.ui.create_connection_flow.state.ConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.CONNECT_NOW
import chad.orionsoft.sendit.utils.Constants.ENTER_CODE_SIGNAL
import chad.orionsoft.sendit.utils.Constants.MIN_SUPPORT_VERSION
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.sendData
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class CCSenderViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val preferenceRepository: PreferenceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _ccSenderState = MutableStateFlow(CCSenderState())
    val ccSenderState = _ccSenderState.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var isOnWlan = false

    private val tag = "CCSenderViewModel"

    private var operationReceiverPort = Constants.DEFAULT_BROADCAST_PORT

    var receiverDetails : ReceiverData? = null

    val connectCode by lazy {
        RuntimeConstants.generateRandomCode()
    }

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
                    broadcastSearch()
                }
            }
        }
    }

    fun onEvent(event: CCSenderEvent) {
        when(event) {
            CCSenderEvent.OnStartConnectionProcess -> {
                val nextState = if(isOnWlan) {
                    receiverDetails = null
                    currentJob = ioScope.launch {
                        broadcastSearch()
                    }
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
                currentJob?.cancel("selected")
                ioScope.launch {
                    receiverDetails = ccSenderState.value.receiverList.find {
                        it.name == event.name
                    }
                    receiverDetails?.let { receiver ->
                        _ccSenderState.update {
                            it.copy(
                                currentStep = ConnectStep.FinalHandshake,
                            )
                        }
                        val deviceInfo = sendSelectSig(receiver.address, operationReceiverPort)
                        if(deviceInfo.success) {
                            checkReceiverInfo(deviceInfo)
                        } else {
                            setError("receiver not responding")
                        }
                    }

                }
            }
        }
    }

    private suspend fun checkReceiverInfo(udpResponse: UDPResponse) {
        val infoStr = udpResponse.response
        val startIndexOfId = infoStr.indexOf("/*")
        val endIndexOfId = infoStr.indexOf("*/")
        val endIndexOfPort = infoStr.indexOf("#")
        val uniId = infoStr.substring(startIndexOfId+2, endIndexOfId)
        val newPort = infoStr.substring(endIndexOfId+2, endIndexOfPort)
        val appVer = infoStr.substring(endIndexOfPort+1)
        Log.d("CreateConnection", "device info : $uniId , $newPort, $appVer")
        operationReceiverPort = newPort.toInt()
        Log.d("CreateConnection", "s6 check receiver")
        delay(1100)
        if(appVer.toInt() < MIN_SUPPORT_VERSION) {
            setError("Receiver using old version, could not continue")
            sendReceiverNotSupported(udpResponse.partnerAddress, operationReceiverPort, "old version")
            return
        }
        val isKnownDevice = false // TODO check if unknown device
        if(!isKnownDevice) {
            _ccSenderState.update {
                it.copy(
                    currentStep = ConnectStep.ShowConnectCode
                )
            }
            val enteredCode = sendEnterCodeRequest(udpResponse.partnerAddress, operationReceiverPort)
            if(enteredCode.success) {
                try {
                    val resStr = enteredCode.response
                    if(resStr.contains(ENTER_CODE_SIGNAL)) {
                        val enteredCode1 = Converters.extractDataInNormalCase(resStr)
                        if(enteredCode1 == "1234") {
                            _ccSenderState.update {
                                it.copy(
                                    currentStep = ConnectStep.FinalHandshake
                                )
                            }
                        } else {
                            setError("Receiver sent wrong code, could not connect")
                        }
                        if(receiverDetails == null) {
                            setError("Receiver details not found")
                        } else {
                            val finalResponse = sendEnterCodeResult(udpResponse.partnerAddress, operationReceiverPort, enteredCode1 == "1234")
                            if(finalResponse.success) {
                                if(finalResponse.response.contains(CONNECT_NOW)) {
                                    val freeSpace = Converters.extractDataInNormalCase(finalResponse.response).toLong()
                                    RuntimeConstants.connectedDeviceDetails = ConnectedDeviceDetails(
                                        receiverDetails?.name ?: "Unknown",
                                        receiverDetails?.address!!,
                                        freeSpace
                                    )
                                    Log.d("CreateConnection", "Connected : ${RuntimeConstants.connectedDeviceDetails}")
                                    _ccSenderState.update {
                                        it.copy(
                                            currentStep = ConnectStep.Connected
                                        )
                                    }
                                }
                            }
                        }
                        return
                    }
                } catch (e:Exception) {
                    Log.e("CreateConnection", "s7_b1 >> Exception receiving code")
                }
            } else {
                setError()
            }
        }
        val freeSpaceRes = sendConnectOkSignal(udpResponse.partnerAddress, operationReceiverPort)
        if(freeSpaceRes.success) {
            try {
                val resStr = freeSpaceRes.response
                if (resStr.contains(CONNECT_NOW)) {
                    val indexOfSpaceStart = resStr.indexOf("/*")
                    val indexOfEnd = resStr.indexOf("*/")
                    val freeSpace = resStr.substring(indexOfSpaceStart+2, indexOfEnd)
                    Log.d("CreateConnection", "s7_a2 >> got free space: $freeSpace")
                    _ccSenderState.update {
                        it.copy(
                            currentStep = ConnectStep.Connected
                        )
                    }
                    return
                }
            } catch (e:Exception) {
                Log.e("CreateConnection", "s7_a2 >> Exception getting free space")
            }
        }
        setError()
    }

    private suspend fun broadcastSearch() {
        val broadcastAddress = obtainBroadcastAddress()
        if(broadcastAddress == null) {
            Log.e(tag, "failed to obtain broadcast address")
            onEvent(CCSenderEvent.OnCancelProcess)
            return
        }
        withContext(Dispatchers.IO) {
            val dSocket = DatagramSocket()
            dSocket.soTimeout = 4000
            while (
                isActive &&
                ccSenderState.value.currentStep == ConnectStep.SearchReceiver &&
                    receiverDetails == null) {
                Log.d("CreateConnection", "s2 -> sending broadcast")
                val result = dSocket.sendData(
                    Constants.BROADCAST_MSG, InetAddress.getByName(broadcastAddress),
                    4444
                )
                println("broadcast result >>> $result")
                if(result.success) {
                    Log.d("CreateConnection", "s3 -> found receiver : ${result.partnerAddress}")
                    addToReceiverList(
                        ReceiverData(
                            result.partnerAddress, extractReceiverNameFromBroadcastRes(result.response)
                        )
                    )
                }
                delay(3000)
            }
        }
    }

    private suspend fun sendSelectSig(toAddress: InetAddress, toPort: Int): UDPResponse {
        // exit from the broadcast loop
        return withContext(Dispatchers.IO) {
            val sendSocket = DatagramSocket()
            Log.d("CreateConnection", "s4 -> sent selected signal")
            val res = sendSocket.sendData(
                "${Constants.RECEIVER_SELECT_MESSAGE}/*${createMyUsername()}*/",
                toAddress,
                toPort
            )
            sendSocket.close()
            Log.d("CreateConnection", "s5 -> received device info: $res")
            res
        }
    }


    private suspend fun sendConnectOkSignal(toAddress: InetAddress, toPort: Int) : UDPResponse {
        return withContext(Dispatchers.IO) {
            val sendSocket = DatagramSocket()
            Log.d("CreateConnection", "s7_a1 -> send connect okay signal")
            val res = sendSocket.sendData("$CONNECT_NOW/*${createMyUsername()}",
                toAddress, toPort)
            return@withContext res
        }
    }

    private fun createMyUsername(): String {
        val uName = preferenceRepository.getUserNameFromPreferences()
        return "$uName - ${Build.BRAND}"
    }

    private suspend fun obtainBroadcastAddress(): String? {
        if (RuntimeConstants.BROAD_ADDR != null) {
            println("broadcast address: ${RuntimeConstants.BROAD_ADDR}")
            return RuntimeConstants.BROAD_ADDR
        }
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val mobDataState = tm.dataState
        withContext(Dispatchers.IO) {
            val dSocket = DatagramSocket()
            while (RuntimeConstants.BROAD_ADDR == null) {
                for (i in 0 until 256) {
                    val host = "192.168.$i.255"
                    if (mobDataState == 0) {
                        dSocket.soTimeout = 15000
                    } else {
                        dSocket.soTimeout = 100
                    }
                    val response = dSocket.sendData(
                        Constants.BROADCAST_MSG,
                        InetAddress.getByName(host),
                        waitForResponse = false,
                        toPort = operationReceiverPort
                    )
                    if (response.success) {
                        println("broadcast address: $host")
                        RuntimeConstants.BROAD_ADDR = host
                        Log.d("CreateConnection", "s1 -> Obtained broadcast address")
                        dSocket.close()
                        return@withContext host
                    }
                }
            }
        }
        return null
    }

    private fun addToReceiverList(receiverData: ReceiverData) {
        val oldList = ccSenderState.value.receiverList.toMutableList()
        val addToList = {
            oldList.add(receiverData)
            _ccSenderState.update {
                it.copy(
                    receiverList = oldList,
                    receiverListToDisplay = oldList.map { rData ->
                        rData.name
                    }
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

    suspend fun sendReceiverNotSupported(toAddress: InetAddress, toPort: Int, reason: String) : UDPResponse {
        return withContext(Dispatchers.IO) {
            val sendSocket = DatagramSocket()
            val res = sendSocket.sendData("${Constants.RECEIVER_NOT_SUPPORTED}/*$reason",
                toAddress, toPort)
            Log.d("CreateConnection", "s6_1 -> send receiver not supported because : $reason")
            return@withContext res
        }
    }

    suspend fun sendEnterCodeRequest(toAddress: InetAddress, toPort: Int) : UDPResponse {
        return withContext(Dispatchers.IO) {
            val sendSocket = DatagramSocket()
            Log.d("CreateConnection", "s7_b1 -> send enter code signal")
            val res = sendSocket.sendData("$ENTER_CODE_SIGNAL/*${createMyUsername()}",
                toAddress, toPort, soTimeout1 = 30000)
            return@withContext res
        }
    }

    suspend fun sendEnterCodeResult(toAddress: InetAddress, toPort: Int, result : Boolean) : UDPResponse {
        return withContext(Dispatchers.IO) {
            val sendSocket = DatagramSocket()
            Log.d("CreateConnection", "s7_b3 -> send enter code result signal")
            val res = sendSocket.sendData(
                "${Constants.CODE_ENTERED_SIGNAL_RESULT}/*$result*/",
                toAddress, toPort
            )
            return@withContext res
        }
    }

    private fun extractReceiverNameFromBroadcastRes(result: String) : String {
        val startIndex = result.indexOf("/*") + 2
        val endIndex = result.indexOf("*/")
        return result.substring(startIndex, endIndex)
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