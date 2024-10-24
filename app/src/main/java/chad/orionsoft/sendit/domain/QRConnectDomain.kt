package chad.orionsoft.sendit.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.model.QRCodeData
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.RuntimeConstants
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRConnectDomain @Inject constructor(
    private val networkObserver: NetworkObserver,
    private val preferenceRepository: PreferenceRepository,
    private val connectProcessRepository: ConnectProcessRepository,
    @ApplicationContext private val context: Context
) {

    private var udpServerPortForThisSession : String? = RuntimeConstants.generateRandomCode(4)

    private val _failedCauseFlow = MutableSharedFlow<Pair<String,String>>()
    val failedCauseFlow = _failedCauseFlow.asSharedFlow()

    fun generateQRString() : String {
        val wifiAddress = networkObserver.getWifiAddress()?.split("/")?.get(0)
        var qrString = ""
        if(!wifiAddress.isNullOrEmpty()) {
            val myColor = Converters.convertColorToString(RuntimeConstants.myColor)
            val operationalPort = generateRandomPortAndStartServer()
            val qrCode = QRCodeData(
                Constants.DEVICE_TYPE_PHONE, wifiAddress, operationalPort, myColor,
                RuntimeConstants.createMyUserName(preferenceRepository), Constants.MY_APP_VERSION
            )
            val gson = Gson()
            val qrJson = gson.toJson(qrCode, QRCodeData::class.java)
            val encryptedQrData = EncryptionDecryption.encrypt(qrJson)
            qrString = Constants.APP_CODE + "-" + encryptedQrData
            println("qr >>> $qrString")
        }
        return qrString
    }

    fun getQRBitmap(msg: String, size : Int) : Bitmap? {
        val result = try {
            MultiFormatWriter().encode(msg, BarcodeFormat.QR_CODE, size, size)
        } catch (e: Exception) {
            Log.e("getQRBitmap", "$e")
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w*h)

        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if(result.get(x,y)) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0,size, 0,0,w,h)
        return bitmap
    }

    private fun generateRandomPortAndStartServer() : Int {
        if(udpServerPortForThisSession == null) {
            udpServerPortForThisSession = RuntimeConstants.generateRandomCode(4)
        }
        val randomPort = udpServerPortForThisSession!!.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            connectProcessRepository
                .startUDPServerIfNotRunning(randomPort)
        }
        return randomPort
    }

    suspend fun processScannedQR(qrContent: String) {
        try {
            val qrCodeDataEncrypted = qrContent.substring(startIndex = (qrContent.indexOf("-") + 1))
            val qrCodeDataJson = EncryptionDecryption.decrypt(qrCodeDataEncrypted)
            println("Qr code json = $qrCodeDataJson")
            val qrCodeData = Gson().fromJson(qrCodeDataJson, QRCodeData::class.java)
            val userName = qrCodeData.userName
            val deviceType = qrCodeData.deviceType
            connectProcessRepository.updateConnectStatus(ConnectStatus.ConnectingDevice.also {
                it.msg = userName
            })
            println("Scanned >>>> Name : $userName")
            val ipAddress = qrCodeData.address
            val port = qrCodeData.port
            println("Scanned >>>> Ip Address : $ipAddress:$port")
            val theirColor = qrCodeData.color
            println("Scanned >>>> Color : $theirColor")
            RuntimeConstants.theirColor = Converters.convertStringToColor(theirColor)
            // My Details
            val myNameAndType = RuntimeConstants.createMyUserName(preferenceRepository)
            val myColor = Converters.convertColorToString(RuntimeConstants.myColor)
            val myFreeSpace = FileIOOperations.getMyFreeSpace(context)
            val success = connectProcessRepository.noteUDPServerAndSendHandshake(
                userName, ipAddress, port,
                Converters.createTripleData(Constants.APP_CODE, myNameAndType, myColor, myFreeSpace))
            if(success) {
                connectProcessRepository.updateConnectStatus(ConnectStatus.DeviceConnected)
            } else {
                connectProcessRepository.markAsDisconnected()
                CoroutineScope(Dispatchers.IO).launch {
                    _failedCauseFlow.emit(
                        Pair("Failed to Connect : $userName",
                            "This device is not in your wifi network, make sure you and $userName are connected on same wifi network")
                    )
                }
            }
        } catch (e:Exception) {
            Log.e("QrCodeScanned", "Exception : $e")
            CoroutineScope(Dispatchers.IO).launch {
                _failedCauseFlow.emit(
                    Pair("Failed to Connect : ",
                        "Something went wrong....")
                )
            }
        }

    }

    suspend fun sendDisconnectSignal() {
        udpServerPortForThisSession = null
        connectProcessRepository.sendDisconnectSignal()
        connectProcessRepository.markAsDisconnected()
    }

    fun stopAllUdpProcess() {
        udpServerPortForThisSession = null
        connectProcessRepository.stopAllUdpProcess()
    }

    fun getPairingDeviceDetails() = connectProcessRepository.getPairingDeviceDetails()
    fun getCurrentConnectState(): ConnectStatus = connectProcessRepository.getCurrentConnectState()
}