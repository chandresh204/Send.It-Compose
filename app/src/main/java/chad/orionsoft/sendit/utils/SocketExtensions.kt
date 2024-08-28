package chad.orionsoft.sendit.utils

import chad.orionsoft.sendit.model.TCPResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.Socket

suspend fun Socket.sendData(msg: String, waitForResponse: Boolean = true) : TCPResult =
    withContext(Dispatchers.IO) {
        try {
            soTimeout = 60000
            val outStream = getOutputStream()
            val bytes = msg.toByteArray()
            outStream.write(bytes, 0, bytes.size)
            if (waitForResponse) {
                return@withContext this@sendData.receiveData()
            } else {
                return@withContext TCPResult(true, null)
            }
        } catch (e: Exception) {
            return@withContext TCPResult(false, "Exception: $e")
        }
    }

suspend fun Socket.receiveData() : TCPResult =
    withContext(Dispatchers.IO) {
        try {
            soTimeout = 60000
            val inStream = getInputStream()
            while (inStream.available() == 0) {
                delay(100)
            }
            val buff = ByteArray(inStream.available())
            val read = inStream.read(buff)
            val resMsg = String(buff, 0, read)
            return@withContext TCPResult(true, resMsg)
        } catch (e: Exception) {
            return@withContext TCPResult(false, "Exception: $e")
        }
    }
