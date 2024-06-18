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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

class UDPOperations @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var tag = "UDPOperation"

    suspend fun obtainBroadcastAddress() : String? {
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

    suspend fun broadcastSearch(broadcastAddress: String) {
        val searchBytes = ("$APP_CODE/*$SEARCH_CODE*/").toByteArray()
        while (true) {
            val recei = sendOnUdp(searchBytes, broadcastAddress, soTimeout = 4000)
            Log.d(tag, "recev: $recei")
        }
    }

    private suspend fun sendOnUdp(bytes: ByteArray, host: String, waitForResponse : Boolean = true,
                                  soTimeout: Int = 15000, receiverPort : Int = RECEIVER_PORT) : String? {
        return withContext(Dispatchers.IO) {
            try {
                val dSocket = DatagramSocket()
                val iAddress = InetAddress.getByName(host)
                val sendPacket = DatagramPacket(bytes, bytes.size, iAddress, receiverPort)
                dSocket.send(sendPacket)
                if(waitForResponse) {
                    dSocket.soTimeout = soTimeout
                    val buff = ByteArray(100)
                    val receiverPacket = DatagramPacket(buff, buff.size)
                    dSocket.receive(receiverPacket)
                    val add = receiverPacket.address
                    return@withContext add.toString()
                } else {
                    return@withContext "Success"
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception on UDP , host: $host --> $e")
                return@withContext null
            }
        }
    }
}