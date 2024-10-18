package chad.orionsoft.sendit.repository

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.domain.FileIOOperations
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.RuntimeConstants
import chad.orionsoft.sendit.utils.getResponse
import chad.orionsoft.sendit.utils.sendData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectProcessRepository @Inject constructor(
    private val networkObserver: NetworkObserver,
    @ApplicationContext private val context: Context
){

    private val tag = "ConnectProcessRepository"

    private var theirUdpServerAddress : InetAddress? = null
    private var theirUdpServerPort : Int? = null
    private var serverOnPort : Int = -1
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

    fun startUDPServerIfNotRunning(port: Int) {
        Log.d(tag, "Starting udp server on port >>> $port")
        if(serverOnPort == port) {
            Log.d(tag, "Udp server already running on $serverOnPort")
            return
        } else {
            serverOnPort = -1
            CoroutineScope(Dispatchers.IO).launch {
                while (serverRunning) {
                    Log.d(tag, "Waiting for server to stop on Port: $serverOnPort")
                    delay(200)
                }
                startUDPServer(port)
            }
        }
    }

    private suspend fun startUDPServer(port: Int) {
        withContext(Dispatchers.IO) {
            serverOnPort = port
            while (serverOnPort > 0) {
                val dSocket = DatagramSocket(port)
                dSocket.soTimeout = 30000
                Log.d(tag, "UDPServer >>> Running")
                serverRunning = true
                val res = dSocket.getResponse()
                if(res.success) {
                    Log.d(tag,"received : $${res.response}")
                    _connectStatusFlow.emit(ConnectStatus.ConnectingDevice)
                    if(res.response.contains(Constants.APP_CODE)) {
                        val resAddress = res.partnerAddress
                        val resPort = res.partnerPort
                        val tripleData = Converters.extractTripleData(res.response)
                        val deviceName = tripleData.first
                        RuntimeConstants.theirColor = Converters.convertStringToColor(tripleData.second)
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
                            val freeSpace = FileIOOperations.getMyFreeSpace(context)
                            val replyBytes = "${Constants.QR_CONNECT_REPLY}/*$freeSpace*/".toByteArray()
                            val replyPacket = DatagramPacket(
                                replyBytes, replyBytes.size, resAddress, resPort
                            )
                            dSocket.send(replyPacket)
                            dSocket.close()
                            delay(500)
                            _connectStatusFlow.emit(ConnectStatus.DeviceConnected)
                        }catch (e:Exception) {
                            dSocket.close()
                            _connectStatusFlow.emit(ConnectStatus.OnWifi)
                        }
                    }
                } else {
                    dSocket.close()
                    delay(500)
                }
            }
            serverOnPort = -1
            serverRunning = false
        }
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
            serverOnPort = -1
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
        serverOnPort = -1
    }

    fun getPairingDeviceDetails() = pairingDeviceDetails

    companion object {

        private var pairingDeviceDetails: ConnectedDeviceDetails? = null

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

        fun markAsDisconnected() {
            updateAndEmitConnectedDeviceDetails(null)
        }
    }
}