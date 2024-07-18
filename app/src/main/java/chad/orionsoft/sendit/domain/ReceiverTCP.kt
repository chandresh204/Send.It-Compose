package chad.orionsoft.sendit.domain

import android.util.Log
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.ServerSocket
import javax.inject.Inject

class ReceiverTCP @Inject constructor() {

    private val tcpScope = CoroutineScope(Dispatchers.IO)
    private var currentJob : Job? = null
    private val receiverPort = 4444 // TODO change to dynamic obtained in connection process
    private val tag = "ReceiverTCP"

    fun startReceiving() {

        currentJob = tcpScope.launch {
            try {
                Log.d(tag, "creating tcp server socket")
                val serverSocket = ServerSocket(receiverPort)
                val receiverSocket = serverSocket.accept()
                val recStream = receiverSocket.getInputStream()
                val sendStream = receiverSocket.getOutputStream()
                Log.d(tag, "Receiver tcp started on port: $receiverSocket")

                while (isActive && recStream.available() == 0) {
                    delay(100)
                }
                val buff = ByteArray(1024)
                val buffSize = recStream.read(buff)
                val modeString = String(buff, 0, buffSize)
                Log.d(tag, "Mode string: $modeString")
            } catch (e: Exception) {
                Log.e(tag, "Exception: $e")
            }
        }
    }
}