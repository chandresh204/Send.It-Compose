package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.TelephonyManager
import android.util.Log
import chad.orionsoft.sendit.utils.Constants.APP_CODE
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
    @ApplicationContext private val context: Context
) {

    private var tag = "UDPOperation"
    private val ioScope = CoroutineScope(Dispatchers.IO)

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
            while (true) {
                val recei = sendOnUdp(searchBytes, broadcastAddress, soTimeout = 4000)
                if(recei?.receiverAddress != null) {
                    channel.send(recei)
                    delay(2000)
                }
            }
        }
        return channel.receiveAsFlow()
    }

    private suspend fun sendOnUdp(bytes: ByteArray, host: String, waitForResponse : Boolean = true,
                                  soTimeout: Int = 15000, receiverPort : Int = RECEIVER_PORT) : UDPResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val dSocket = DatagramSocket()
                val iAddress = InetAddress.getByName(host)
                val sendPacket = DatagramPacket(bytes, bytes.size, iAddress, receiverPort)
                dSocket.send(sendPacket)
                if(waitForResponse) {
                    dSocket.soTimeout = soTimeout
                    val buff = ByteArray(1024)
                    val receiverPacket = DatagramPacket(buff, buff.size)
                    dSocket.receive(receiverPacket)
                    val add = receiverPacket.address
                    val res = String(buff,0, buff.size)
                    return@withContext UDPResponse(add, res)
                } else {
                    return@withContext UDPResponse(null, null)
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception on UDP , host: $host --> $e")
                return@withContext null
            }
        }
    }
}

data class UDPResponse(
    val receiverAddress: InetAddress?,
    val response: String?
)