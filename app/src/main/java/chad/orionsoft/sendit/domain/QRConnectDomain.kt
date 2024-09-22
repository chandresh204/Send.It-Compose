package chad.orionsoft.sendit.domain

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRConnectDomain @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val preferenceRepository: PreferenceRepository,
    private val connectProcessRepository: ConnectProcessRepository,
    @ApplicationContext private val context: Context
) {



    private val _failedCauseFlow = MutableSharedFlow<Pair<String,String>>()
    val failedCauseFlow = _failedCauseFlow.asSharedFlow()


    fun openQRServerAndShowQR() : String {
       return generateQRString()
    }

    private fun generateQRString() : String {
        val wifiAddress = networkObserver.getWifiAddress()?.split("/")?.get(0)
        var qrCode = ""
        if(!wifiAddress.isNullOrEmpty()) {
            val myColor = preferenceRepository.getMyColorCode()
            val operationalPort = generateRandomPortAndStartServer()
            val tripleData = Converters.createTripleData(Constants.APP_CODE,
                RuntimeConstants.createMyUserName(preferenceRepository),
                "$wifiAddress:$operationalPort",
                myColor)
            qrCode = tripleData
            println("qr >>> $qrCode")
        }
        return qrCode
    }

    private fun generateRandomPortAndStartServer() : String {
        val randomPort = RuntimeConstants.generateRandomCode(4)
        CoroutineScope(Dispatchers.IO).launch {
            connectProcessRepository
                .startUDPServer(randomPort.toInt(), FileIOOperations.getMyFreeSpace(context)
                ).collect { newStatus ->
                    if(newStatus == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Device failed to Connect", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        connectProcessRepository.updateConnectStatus(newStatus)
                    }
            }
        }
        return randomPort
    }

    suspend fun processScannedQR(qrContent: String) {
        val previousState = connectProcessRepository.getCurrentNetworkState()
        val qrTriple = Converters.extractTripleData(qrContent)
        val nameAndType = qrTriple.first
        connectProcessRepository.updateConnectStatus(ConnectStatus.ConnectingDevice.also {
            it.msg = nameAndType
        })
        println("Scanned >>>> Name : $nameAndType")
        val ipAddressWithPort = qrTriple.second
        println("Scanned >>>> Ip Address : $ipAddressWithPort")
        val ipAndPort = Converters.separateAddressAndPort(ipAddressWithPort)
        val color = qrTriple.third
        println("Scanned >>>> Color : $color")
        // My Details
        val myNameAndType = RuntimeConstants.createMyUserName(preferenceRepository)
        val myColor = RuntimeConstants.myColor
        val myFreeSpace = FileIOOperations.getMyFreeSpace(context)
        val success = connectProcessRepository.noteUDPServerAndSendHandshake(
            nameAndType, Constants.DEVICE_TYPE_PHONE, Color.Red, ipAndPort.first, ipAndPort.second,
            Converters.createTripleData(Constants.APP_CODE, myNameAndType, myColor, myFreeSpace))
        if(success) {
            connectProcessRepository.updateConnectStatus(ConnectStatus.DeviceConnected)
        } else {
            connectProcessRepository.updateConnectStatus(previousState)
            CoroutineScope(Dispatchers.IO).launch {
                _failedCauseFlow.emit(
                    Pair("Failed to Connect : $nameAndType",
                        "This device is not in your wifi network, make sure you and $nameAndType are connected on same wifi network")
                )
            }
        }
    }

    fun stopAllUdpProcess() {
        connectProcessRepository.stopAllUdpProcess()
    }

    fun getPairingDeviceDetails() = connectProcessRepository.getPairingDeviceDetails()
    fun getCurrentNetworkState(): ConnectStatus = connectProcessRepository.getCurrentNetworkState()
}