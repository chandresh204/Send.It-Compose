package chad.orionsoft.sendit.utils

import android.util.Log
import chad.orionsoft.sendit.model.UDPResponse
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object DatagramSocketExtensions {

    fun DatagramSocket.getResponse(): UDPResponse {
        Log.d("DatagramSocket", "receiving on ${this.port}....")
        return try {
            val buff = ByteArray(1024)
            val receivePacket = DatagramPacket(buff, buff.size)
            this.receive(receivePacket)
            val senderAddress = receivePacket.address
            val senderPort = receivePacket.port
            val response = String(buff, 0, receivePacket.length)
            Log.d("DatagramSocket", "received : $response --> on ${this.port}")
            UDPResponse(true, senderAddress, senderPort, response)
        } catch (e: Exception) {
            Log.e("DatagramSocket", "exception while receiving: $e")
            UDPResponse(false, InetAddress.getLocalHost(), -1, e.toString())
        }
    }

    fun DatagramSocket.sendData(
        msg: String,
        toAddress: InetAddress,
        toPort: Int,
        waitForResponse: Boolean = true
    ): UDPResponse {
        val payload = msg.toByteArray()
        Log.d("DatagramSocket", "sending: $msg")
        return try {
            val sendPacket = DatagramPacket(payload, payload.size, toAddress, toPort)
            this.send(sendPacket)
            if (waitForResponse) {
                this.soTimeout = soTimeout
                val buff = ByteArray(1024)
                val receiverPacket = DatagramPacket(buff, buff.size)
                this.receive(receiverPacket)
                val add = receiverPacket.address
                val port = receiverPacket.port
                val res = String(buff, 0, receiverPacket.length)
                Log.d("DatagramSocket", "reply: $res --> $msg")
                return UDPResponse(true, add, port, res)
            }
            Log.d("DatagramSocket", "sent --> $msg")
            UDPResponse(true, InetAddress.getLocalHost(), -1, "sent successfully")
        } catch (e: Exception) {
            Log.e("DatagramSocket", "exception during send $e")
            UDPResponse(false, InetAddress.getLocalHost(), -1, "error: $e")
        }
    }
}