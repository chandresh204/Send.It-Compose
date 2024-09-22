package chad.orionsoft.sendit.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.getResponse
import chad.orionsoft.sendit.utils.sendData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectProcessRepository @Inject constructor(
    private val networkObserver: NetworkObserver
){

    private val tag = "ConnectProcessRepository"

    private var theirUdpServerAddress : InetAddress? = null
    private var theirUdpServerPort : Int? = null
    private var keepUdpServer: Boolean  = false
    private var serverRunning : Boolean = false

    private var connectStatus : ConnectStatus = ConnectStatus.NoNetwork

    init {
        CoroutineScope(Dispatchers.IO).launch {
            networkObserver.networkStatus.collectLatest {
                updateConnectStatus(it)
            }
        }
    }

    fun getCurrentNetworkState() : ConnectStatus = connectStatus

    fun updateConnectStatus(newStatus: ConnectStatus) {
        connectStatus = newStatus
        CoroutineScope(Dispatchers.IO).launch {
            _connectStatusFlow.emit(newStatus)
        }
    }

    fun startUDPServer(port: Int, myFreeSpace: Long) : Flow<ConnectStatus?> {
        val statusFlow = Channel<ConnectStatus?>()
        CoroutineScope(Dispatchers.IO).launch {
            while(serverRunning) {
                Log.d(tag, "old server process still running")
                delay(100)
            }
            keepUdpServer = true
            while (keepUdpServer) {
                val dSocket = DatagramSocket(port)
                dSocket.soTimeout = 30000
                Log.d(tag, "UDPServer >>> Running")
                serverRunning = true
                val res = dSocket.getResponse()
                if(res.success) {
                    Log.d(tag,"received : $${res.response}")
                    statusFlow.send(ConnectStatus.ConnectingDevice)
                    if(res.response.contains(Constants.APP_CODE)) {
                        val resAddress = res.partnerAddress
                        val resPort = res.partnerPort
                        val tripleData = Converters.extractTripleData(res.response)
                        val deviceName = tripleData.first
                        val deviceColor = tripleData.second
                        val deviceFree = tripleData.third
                        val connectedDetails = ConnectedDeviceDetails(
                            deviceName,
                            resAddress,
                            Constants.DEVICE_TYPE_PHONE,
                            Color.Red,
                            deviceFree.toLong(),
                            port
                        )
                        Log.d(tag, "Device details: $connectedDetails")
                        updateAndEmitConnectedDeviceDetails(connectedDetails)
                        try {
                            val replyBytes = "${Constants.QR_CONNECT_REPLY}/*$myFreeSpace*/".toByteArray()
                            val replyPacket = DatagramPacket(
                                replyBytes, replyBytes.size, resAddress, resPort
                            )
                            dSocket.send(replyPacket)
                            dSocket.close()
                            delay(500)
                            statusFlow.send(ConnectStatus.DeviceConnected)
                        }catch (e:Exception) {
                            dSocket.close()
                            statusFlow.send(null)
                        }
                    }
                } else {
                    dSocket.close()
                    delay(500)
                }
            }
            serverRunning = false
        }
        return statusFlow.receiveAsFlow()
    }

    suspend fun noteUDPServerAndSendHandshake(
        userName: String, deviceType: String, color: Color,
        inetAddressStr: String, port: Int, connectRequest: String
    ) : Boolean {
        return withContext(Dispatchers.IO) {
            theirUdpServerAddress = InetAddress.getByName(inetAddressStr)
            theirUdpServerPort = port
            val dSocket = DatagramSocket()
            val reply = dSocket.sendData(connectRequest, theirUdpServerAddress!!, port)
            Log.d(tag, "ServerReply >>> $reply")
            keepUdpServer = false
            if(!reply.success) {
                return@withContext false
            } else {
                val freeSpace = Converters.extractDataInNormalCase(reply.response)
                val newDetails = ConnectedDeviceDetails(
                    userName, InetAddress.getByName(inetAddressStr), Constants.DEVICE_TYPE_PHONE,
                    Color.Red, freeSpace.toLong(), port
                )
                Log.d(tag, "Connected Device: $newDetails ")
                updateAndEmitConnectedDeviceDetails(newDetails)
                Log.d(tag, "Stopping UDP server")
                return@withContext true
            }
        }
    }

    fun stopAllUdpProcess() {
        Log.d(tag, "Stopping UDP server")
        keepUdpServer = false
    }

    fun getPairingDeviceDetails() = pairingDeviceDetails

    companion object {

        private var pairingDeviceDetails : ConnectedDeviceDetails? = null

        private val _connectionUpdateChannel = MutableSharedFlow<ConnectedDeviceDetails?>()
        val connectionUpdateReceiver = _connectionUpdateChannel.asSharedFlow()

        private val _connectStatusFlow = MutableSharedFlow<ConnectStatus>()
        val connectStatusFlow = _connectStatusFlow.asSharedFlow()

        fun getPairingDeviceDetails() = pairingDeviceDetails

        fun updateAndEmitConnectedDeviceDetails(newDetails: ConnectedDeviceDetails?) {
            pairingDeviceDetails = newDetails
            CoroutineScope(Dispatchers.IO).launch {
                _connectionUpdateChannel.emit(pairingDeviceDetails)
            }
        }
    }
}