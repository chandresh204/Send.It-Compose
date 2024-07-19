package chad.orionsoft.sendit.domain

import android.util.Log
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket

object TcpSender {

    private lateinit var sendSocket : Socket
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private const val TAG =  "TcpSender"

    fun init(toAddress: InetAddress, toPort: Int) {
        ioScope.launch {
            sendSocket = Socket(toAddress, toPort)
            sendSocket.soTimeout = 60000
        }
    }

    fun sendData(byteArray: ByteArray, waitForResponse : Boolean = true) : TCPResponse {
            return try {
                val outStream = sendSocket.getOutputStream()
                outStream.write(byteArray)
                if(waitForResponse) {
                    val intStream = sendSocket.getInputStream()
                    val buff = ByteArray(1024)
                    val read = intStream.read(buff)
                    val response = String(buff, 0, read)
                    TCPResponse(true, response, null)
                } else {
                    TCPResponse(true, "sent", null)
                }
            }catch (e: Exception) {
                Log.e(TAG, "Error while send data: $e")
                TCPResponse(false, "failed", "$e")
            }
    }

    fun sendStream(inStream: InputStream) : Flow<String> {
        val updateChannel = Channel<String>()
        ioScope.launch {
            try {
                val outStream = sendSocket.getOutputStream()
                var totalSent = 0L
                var loops = 0
                while (true) {
                    loops++
                    val buff = ByteArray(4096)
                    val bytesRead = inStream.read(buff)
                    if (bytesRead <= 0) break
                    outStream.write(buff,0, bytesRead)
                    totalSent += bytesRead
                    if(loops % 100 == 0) updateChannel.send("Sent : ${Converters.formatDataString(totalSent)}")
                }
                updateChannel.send("Finished : ${Converters.formatDataString(totalSent)}")
            } catch (e: Exception) {
                Log.e(TAG, "error sending stream : $e")
                updateChannel.send("Error :$e")
            }
        }
        return updateChannel.receiveAsFlow()
    }
}

data class TCPResponse(val succes : Boolean, val response: String, val error: String?)