package chad.orionsoft.sendit.repository

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.domain.FileIOOperations
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.domain.NotificationDomain
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
import kotlin.random.Random

@Singleton
class ConnectProcessRepository @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val notificationDomain: NotificationDomain,
    @ApplicationContext private val context: Context
){

    private val tag = "ConnectProcessRepository"

    private var theirUdpServerAddress : InetAddress? = null
    private var theirUdpServerPort : Int? = null
    private var serverOnPort : Int = -1
    private var serverRunning : Boolean = false

    private var connectStatus : ConnectStatus = ConnectStatus.NoNetwork
    private var lastNetworkStatus : ConnectStatus = connectStatus
    private var pairingDeviceDetails: ConnectedDeviceDetails? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            networkObserver.networkStatus.collectLatest {
                lastNetworkStatus = it
                updateConnectStatus(it)
            }
        }
    }

    fun getCurrentConnectState() : ConnectStatus = connectStatus

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
            serverOnPort = port
            CoroutineScope(Dispatchers.IO).launch {
                startUDPServer(port)
            }
        }
    }

    private suspend fun startUDPServer(port: Int) {
        withContext(Dispatchers.IO) {
            val dSocket = DatagramSocket(port)
            dSocket.soTimeout = 30000
            while (serverOnPort == port) {
                Log.d(tag, "UDPServer >>> Running on $port")
                serverRunning = true
                val res = dSocket.getResponse()
                if(res.success) {
                    Log.d(tag,"received : ${res.response}")
                    _connectStatusFlow.emit(ConnectStatus.ConnectingDevice)
                    val resAddress = res.partnerAddress
                    val resPort = res.partnerPort
                    when {
                        res.response.contains(Constants.APP_CODE) -> {
                            val tripleData = Converters.extractTripleData(res.response)
                            val deviceName = tripleData.first
                            RuntimeConstants.theirColor = Converters.convertStringToColor(tripleData.second)
                            val deviceFree = tripleData.third
                            val connectedDetails = ConnectedDeviceDetails(
                                deviceName,
                                resAddress,
                                Constants.DEVICE_TYPE_PHONE,
                                RuntimeConstants.theirColor,
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
                                delay(500)
                                _connectStatusFlow.emit(ConnectStatus.DeviceConnected)
                            }catch (e:Exception) {
                                _connectStatusFlow.emit(ConnectStatus.OnWifi)
                            }
                        }
                        res.response.contains(Constants.UDP_DISCONNECT) -> {
                            Log.d(tag, "Disconnect signal received")
                            markAsDisconnected()
                            sendGenericReply(dSocket, resAddress, resPort)
                        }
                        res.response.contains(Constants.UDP_IS_PRESENT) -> {
                            Log.d(tag, "ping signal received")
                            sendGenericReply(dSocket, resAddress, resPort)
                        }
                    }
                } else {
                    delay(500)
                }
            }
            dSocket.close()
            Log.d(tag, "Stopped udp server on $port")
            serverRunning = false
        }
    }

    private fun sendGenericReply(dSocket : DatagramSocket, resAddress: InetAddress, resPort: Int) {
        val replyBytes = Constants.APP_CODE.toByteArray()
        val replyPacket = DatagramPacket(
            replyBytes, replyBytes.size, resAddress, resPort
        )
        dSocket.send(replyPacket)
    }

    suspend fun noteUDPServerAndSendHandshake(
        userName: String,
        inetAddressStr: String, port: Int, connectRequest: String
    ) : Boolean {
        return withContext(Dispatchers.IO) {
            theirUdpServerAddress = InetAddress.getByName(inetAddressStr)
            theirUdpServerPort = port
            val dSocket = DatagramSocket()
            val reply = dSocket.sendData(connectRequest, theirUdpServerAddress!!, port)
            Log.d(tag, "ServerReply >>> $reply")
            if(!reply.success) {
                return@withContext false
            } else {
                val freeSpace = Converters.extractDataInNormalCase(reply.response)
                val newDetails = ConnectedDeviceDetails(
                    userName, InetAddress.getByName(inetAddressStr), Constants.DEVICE_TYPE_PHONE,
                    Color.Red, freeSpace.toLong(), port
                )
                Log.d(tag, "Connected Device: $newDetails ")
                if((theirUdpServerPort ?: 0) <= 0) {
                    Log.e(tag, "Paring device, their port address not valid : $theirUdpServerPort")
                    return@withContext false
                }
                updateAndEmitConnectedDeviceDetails(newDetails)
                startUDPServerIfNotRunning(theirUdpServerPort!!)
                Log.d(tag, "Stopping UDP server")
                return@withContext true
            }
        }
    }

    // TODO use this method
    suspend fun pingConnectedDevice() {
        withContext(Dispatchers.IO) {
            try {
                pairingDeviceDetails?.let {
                    val dSocket = DatagramSocket()
                    dSocket.soTimeout = 10000
                    val reply = dSocket.sendData(Constants.UDP_IS_PRESENT,
                        it.address, it.serverPort)
                    if(!reply.success) {
                        Log.e(tag, "Ping to device failed, setting not connected")
                        markAsDisconnected()
                    } else {
                        Log.d(tag, "Ping to device : Present")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Ping to device failed: $e")
                markAsDisconnected()
            }
        }
    }

    suspend fun sendDisconnectSignal() {
        if(serverOnPort <= 0) {
            Log.e(tag, "Disconnect signal : Server not on port")
            return
        }
        if(pairingDeviceDetails?.address == null) {
            Log.e(tag, "Disconnect signal : No device address")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val dSocket = DatagramSocket()
                dSocket.sendData(
                    Constants.UDP_DISCONNECT,
                    pairingDeviceDetails?.address!!,
                    serverOnPort
                )
            } catch (e: Exception) {
                serverOnPort = -1
                Log.e(tag, "Disconnect Signal : sending failed: $e")
            }
        }
    }

    fun stopAllUdpProcess() {
        Log.d(tag, "Stopping UDP server")
        serverOnPort = -1
    }

    fun getPairingDeviceDetails() = pairingDeviceDetails

    private fun updateAndEmitConnectedDeviceDetails(newDetails: ConnectedDeviceDetails?) {
        pairingDeviceDetails = newDetails
        CoroutineScope(Dispatchers.IO).launch {
            _connectionUpdateChannel.emit(pairingDeviceDetails)
        }
    }

    fun markAsDisconnected() {
        stopAllUdpProcess()
        updateAndEmitConnectedDeviceDetails(null)
        updateConnectStatus(lastNetworkStatus)
        notificationDomain.closeNotification(Constants.SERVER_WM_NOTIFICATION_ID)
        notificationDomain.closeNotification(Constants.SENDER_WM_NOTIFICATION_ID)
        notificationDomain.closeNotification(Constants.SERVER_WM_ERR_NOTIFICATION_ID)
        notificationDomain.closeNotification(Constants.SENDER_WM_ERR_NOTIFICATION_ID)
        notificationDomain.showNotification(
            Random.nextInt(500), "Disconnected",
            "Not Connected to Any Device"
        )
    }

    companion object {

        private val _connectionUpdateChannel = MutableSharedFlow<ConnectedDeviceDetails?>()
        val connectionUpdateReceiver = _connectionUpdateChannel.asSharedFlow()

        private val _connectStatusFlow = MutableSharedFlow<ConnectStatus>()
        val connectStatusFlow = _connectStatusFlow.asSharedFlow()
    }
}