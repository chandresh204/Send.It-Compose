package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.Constants.APP_CODE
import chad.orionsoft.sendit.utils.Constants.ASK_CODE_SIG
import chad.orionsoft.sendit.utils.Constants.CONNECT_SIG
import chad.orionsoft.sendit.utils.Constants.NEXT_SIG_END
import chad.orionsoft.sendit.utils.Constants.RECEIVER_PORT
import chad.orionsoft.sendit.utils.Constants.SEARCH_CODE
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

class UDPOperations @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceRepository: PreferenceRepository
) {

    private var tag = "UDPOperation"
    private val ioScope = CoroutineScope(Dispatchers.IO)
    var keepBroadcasting = true

    suspend fun obtainBroadcastAddress() : String? {
        if(RuntimeConstants.BROAD_ADDR != null) {
            println("broadcast address: ${RuntimeConstants.BROAD_ADDR}")
            return RuntimeConstants.BROAD_ADDR
        }
        val searchBytes = ("$APP_CODE/*$SEARCH_CODE*/").toByteArray()
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val mobDataState = tm.dataState
        while (RuntimeConstants.BROAD_ADDR == null) {
            for (i in 0 until 256) {
                val host = "192.168.$i.255"
                val response = if (mobDataState == 0) {
                    sendOnUdp(searchBytes, host, waitForResponse = false)
                } else {
                    sendOnUdp(searchBytes, host, soTimeout = 100)
                }
                if (response != null) {
                    println("broadcast address: $host")
                    RuntimeConstants.BROAD_ADDR = host
                    return host
                }
            }
        }
        return null
    }

    suspend fun broadcastSearch(broadcastAddress: String): Flow<UDPResponse> {
        val channel = Channel<UDPResponse>()
        val searchBytes = ("$APP_CODE/*$SEARCH_CODE*/").toByteArray()
        ioScope.launch {
            while (keepBroadcasting) {
                val recei = sendOnUdp(searchBytes, broadcastAddress, soTimeout = 4000)
                if(recei?.receiverAddress != null) {
                    channel.send(recei)
                    delay(2000)
                }
            }
        }
        return channel.receiveAsFlow()
    }

    suspend fun receiverHandshake(receiverAddress: InetAddress) {
        keepBroadcasting = false
        val nextSig = "$APP_CODE#${createMyUsername()}$NEXT_SIG_END".toByteArray()
        sendOnUdp(nextSig, RuntimeConstants.BROAD_ADDR, waitForResponse = false)
        delay(1000)
        val connectSig = CONNECT_SIG.toByteArray()
        val response = sendOnUdp(connectSig, receiverAddress)
        val connSig = response?.response ?: "/*0*/0@"
        val freeIndex = connSig.indexOf("/*")
        val verIndex = connSig.indexOf("*/")
        val endIndex = connSig.indexOf("@")
        val mac = connSig.substring(0, freeIndex)
        val free = connSig.substring(freeIndex+2, verIndex)
        val ver = connSig.substring(verIndex+2, endIndex)
        println("receiver handshake response: mac: $mac, free: $free, ver: $ver")
        // TODO check if old device
        // TODO check for old version
        var oldDevice = false // assuming
        if(!oldDevice) {
            val askCodeSignal = ASK_CODE_SIG.toByteArray()
            val codeRes = sendOnUdp(askCodeSignal, receiverAddress, soTimeout = 30000)
            println("receiver code response: ${codeRes?.response}")
        }
    }

    private fun createMyUsername() : String {
        val uName = preferenceRepository.getUserNameFromPreferences()
        return "$uName - ${Build.BRAND}"
    }

    private suspend fun sendOnUdp(bytes: ByteArray, receiverAddress: InetAddress?, waitForResponse : Boolean = true,
                                  soTimeout: Int = 15000, receiverPort : Int = RECEIVER_PORT) : UDPResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val dSocket = DatagramSocket()
                val sendPacket = DatagramPacket(bytes, bytes.size, receiverAddress, receiverPort)
                dSocket.send(sendPacket)
                if(waitForResponse) {
                    dSocket.soTimeout = soTimeout
                    val buff = ByteArray(1024)
                    val receiverPacket = DatagramPacket(buff, buff.size)
                    dSocket.receive(receiverPacket)
                    val add = receiverPacket.address
                    val res = String(buff,0, receiverPacket.length)
                    dSocket.close()
                    return@withContext UDPResponse(add, res)
                } else {
                    dSocket.close()
                    return@withContext UDPResponse(InetAddress.getLocalHost(), null)
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception on UDP , host: $receiverAddress --> $e")
                return@withContext null
            }
        }
    }
    private suspend fun sendOnUdp(bytes: ByteArray, host: String?, waitForResponse : Boolean = true,
                                  soTimeout: Int = 15000, receiverPort : Int = RECEIVER_PORT) : UDPResponse? {
        return withContext(Dispatchers.IO) {
            val iAddress = InetAddress.getByName(host)
            sendOnUdp(bytes, iAddress, waitForResponse, soTimeout, receiverPort)
        }
    }
}

data class UDPResponse(
    val receiverAddress: InetAddress,
    val response: String?
)