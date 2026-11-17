package chad.orionsoft.sendit.repository

import android.content.Context
import android.util.Log
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.AppLogger
import chad.orionsoft.sendit.domain.FileIOOperations
import chad.orionsoft.sendit.domain.NetworkObserver
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.model.DeviceHandshakeData
import chad.orionsoft.sendit.model.QrConnectionResult
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.RuntimeConstants
import chad.orionsoft.sendit.utils.getResponse
import chad.orionsoft.sendit.utils.sendData
import com.google.gson.Gson
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
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class ConnectProcessRepository @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val notificationDomain: NotificationDomain,
    @param:ApplicationContext private val context: Context,
    private val appLogger: AppLogger
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
                if(it == ConnectStatus.NoNetwork) {
                    markAsDisconnected()
                } else {
                    updateConnectStatus(it)
                }
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
        appLogger.log("start UDP Server on port : $port")
        Log.d(tag, "Starting udp server on port >>> $port")
        if(serverOnPort == port) {
            Log.d(tag, "Udp server already running on $serverOnPort")
            appLogger.log("Udp server already running on $serverOnPort")
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
                    appLogger.log("$tag >> received : ${res.response}")
                    _connectStatusFlow.emit(ConnectStatus.ConnectingDevice)
                    val resAddress = res.partnerAddress
                    val resPort = res.partnerPort
                    when {
                        res.response.contains(Constants.APP_CODE) -> {
                            try {
                                val deviceHandshakeJson = res.response.split("===")[1]
                                val deviceHandshakeData = Gson().fromJson(deviceHandshakeJson, DeviceHandshakeData::class.java)
                                val deviceName = deviceHandshakeData.name
                                val deviceFree = deviceHandshakeData.freeSpace
                                val appVersion = deviceHandshakeData.appVersion
                                RuntimeConstants.theirColor = Converters.convertStringToColor(deviceHandshakeData.color)
                                if(appVersion < Constants.MIN_SUPPORT_VERSION) {
                                    emitConnectionFailedCause(
                                        Pair(
                                        context.getString(R.string.failed_to_connect),
                                        context.getString(R.string.failed_to_connect_low_version, deviceName)
                                        )
                                    )
                                    Log.d(tag, "Sending not compatible reply")
                                    appLogger.log("$tag >> sending not compatible reply")
                                    sendVersionNotCompatibleReply(dSocket, resAddress, resPort)
                                    continue
                                }
                                val connectedDetails = ConnectedDeviceDetails(
                                    deviceName,
                                    resAddress,
                                    Constants.DEVICE_TYPE_PHONE,
                                    RuntimeConstants.theirColor,
                                    deviceFree,
                                    port
                                )
                                Log.d(tag, "Device details: $connectedDetails")
                                updateAndEmitConnectedDeviceDetails(connectedDetails)
                                val freeSpace = FileIOOperations.getMyFreeSpace(context)
                                val replyBytes = "${Constants.QR_CONNECT_REPLY}/*$freeSpace*/".toByteArray()
                                val replyPacket = DatagramPacket(
                                    replyBytes, replyBytes.size, resAddress, resPort
                                )
                                dSocket.send(replyPacket)
                                delay(500.milliseconds)
                                _connectStatusFlow.emit(ConnectStatus.DeviceConnected)
                            }catch (e:Exception) {
                                appLogger.log("$tag >> exception : $e, so setting ConnectStatus to OnWifi")
                                _connectStatusFlow.emit(ConnectStatus.OnWifi)
                            }
                        }
                        res.response.contains(Constants.UDP_DISCONNECT) -> {
                            Log.d(tag, "Disconnect signal received")
                            appLogger.log("$tag >> Disconnect signal received")
                            markAsDisconnected()
                            sendGenericReply(dSocket, resAddress, resPort)
                        }
                        res.response.contains(Constants.UDP_IS_PRESENT) -> {
                            Log.d(tag, "ping signal received")
                            appLogger.log("$tag >> ping signal received")
                            sendGenericReply(dSocket, resAddress, resPort)
                        }
                    }
                } else {
                    delay(500.milliseconds)
                }
            }
            dSocket.close()
            Log.d(tag, "Stopped udp server on $port")
            appLogger.log("$tag >> Stopped udp server on $port")
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

    private fun sendVersionNotCompatibleReply(dSocket : DatagramSocket, resAddress: InetAddress, resPort: Int) {
        val replyBytes = Constants.UDP_VERSION_INCOMPATIBLE.toByteArray()
        val replyPacket = DatagramPacket(
            replyBytes, replyBytes.size, resAddress, resPort
        )
        dSocket.send(replyPacket)
    }

    suspend fun noteUDPServerAndSendHandshake(
        userName: String, colorString: String, // other device username and color required
        inetAddressStr: String, port: Int, connectRequest: String
    ) : QrConnectionResult {
        return withContext(Dispatchers.IO) {
            theirUdpServerAddress = InetAddress.getByName(inetAddressStr)
            theirUdpServerPort = port
            val dSocket = DatagramSocket()
            val reply = dSocket.sendData(connectRequest, theirUdpServerAddress!!, port)
            Log.d(tag, "ServerReply >>> $reply")
            if(!reply.success) {
                if(reply.response.contains(Constants.UDP_VERSION_INCOMPATIBLE)) {
                    appLogger.log("$tag >> Server reply incompatible version")
                    return@withContext QrConnectionResult(
                        false,
                        context.getString(R.string.failed_to_connect_low_version)
                    )
                }
                appLogger.log("$tag >> Server reply not in network")
                return@withContext QrConnectionResult(false,
                    context.getString(R.string.failed_to_connect_diff_wifi, userName))
            } else {
                val freeSpace = Converters.extractDataInNormalCase(reply.response)
                val newDetails = ConnectedDeviceDetails(
                    userName, InetAddress.getByName(inetAddressStr), Constants.DEVICE_TYPE_PHONE,
                    Converters.convertStringToColor(colorString), freeSpace.toLong(), port
                )
                Log.d(tag, "Connected Device: $newDetails ")
                appLogger.log("$tag >> Connected Device")
                if((theirUdpServerPort ?: 0) <= 0) {
                    Log.e(tag, "Paring device, their port address not valid : $theirUdpServerPort")
                    appLogger.log("$tag >> Paring device, their port address not valid : $theirUdpServerPort")
                    return@withContext QrConnectionResult(
                        false,
                        context.getString(R.string.failed_to_connect_invalid_port)
                    )
                }
                updateAndEmitConnectedDeviceDetails(newDetails)
                startUDPServerIfNotRunning(theirUdpServerPort!!)
                Log.d(tag, "Stopping UDP server")
                appLogger.log("$tag >> Stopping udp server")
                return@withContext QrConnectionResult(true, "success")
            }
        }
    }

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
                        appLogger.log("$tag >> Ping to device failed, setting not connected")
                        markAsDisconnected()
                    } else {
                        Log.d(tag, "Ping to device : Present")
                        appLogger.log("$tag >> Ping to device : Present")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Ping to device failed: $e")
                appLogger.log("$tag >> Ping to device failed, setting disconected: $e")
                markAsDisconnected()
            }
        }
    }

    suspend fun sendDisconnectSignal() {
        if(serverOnPort <= 0) {
            Log.e(tag, "Disconnect signal : Server not on port")
            appLogger.log("$tag >> Disconnect signal : Server not on port")
            return
        }
        if(pairingDeviceDetails?.address == null) {
            Log.e(tag, "Disconnect signal : No device address")
            appLogger.log("$tag >> Disconnect signal : No device address")
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
                appLogger.log("$tag >> Disconnect Signal : sending failed: $e")
            }
        }
    }

    fun stopAllUdpProcess() {
        Log.d(tag, "Stopping UDP server")
        appLogger.log("$tag >> Stopping udp server, serverOnPort -1")
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
            Constants.DEVICE_DISCONNECTED_NOTIFICATION_ID, "Disconnected",
            "Not Connected to Any Device"
        )
    }

    fun emitConnectionFailedCause(cause : Pair<String,String>) {
        CoroutineScope(Dispatchers.IO).launch {
            _failedCauseFlow.emit(cause)
        }
        updateConnectStatus(lastNetworkStatus)
    }

    companion object {

        private val _connectionUpdateChannel = MutableSharedFlow<ConnectedDeviceDetails?>()
        val connectionUpdateReceiver = _connectionUpdateChannel.asSharedFlow()

        private val _connectStatusFlow = MutableSharedFlow<ConnectStatus>()
        val connectStatusFlow = _connectStatusFlow.asSharedFlow()

        private val _failedCauseFlow = MutableSharedFlow<Pair<String,String>>()
        val failedCauseFlow = _failedCauseFlow.asSharedFlow()
    }
}