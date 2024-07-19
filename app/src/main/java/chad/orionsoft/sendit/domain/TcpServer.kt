package chad.orionsoft.sendit.domain

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import java.net.ServerSocket
import android.content.ContentValues
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import java.net.Socket

object TcpServer {

    private lateinit var serverSocket: ServerSocket
    private lateinit var recSocket : Socket
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val tag = "TcpServer"

    @SuppressLint("NewApi")
    fun start(onPort: Int, contentResolver: ContentResolver) : Flow<String> {  // TODO remove contentResolver dependency
        val updateChannel = Channel<String>()
        ioScope.launch {
            try {
                Log.d(tag, "Starting tcp server")
                serverSocket = ServerSocket(onPort)
                recSocket = serverSocket.accept()
                recSocket.soTimeout = 60000
                Log.d(tag, "Receiving on tcp")
                val inStream = recSocket.getInputStream()
                val outStream = recSocket.getOutputStream()
                while (isActive) {
                    while (isActive && inStream.available() == 0) {
                        delay(100)
                    }
                    val buff = ByteArray(inStream.available())
                    val read = inStream.read(buff)
                    val msg = String(buff,0, read)
                    Log.d(tag, "Received: $msg")
                    updateChannel.send("Received: $msg")
                    if( msg.contains(Constants.TCP_MODE_STR)) {
                        val modeStr = Converters.extractDataInNormalCase(msg)
                        when(modeStr) {
                            Constants.MODE_TYPE_FILE -> {
                                Log.d(tag, "File will be received here")
                                updateChannel.send("waiting for file to receive")
                                receiveFileOverTCP(recSocket, updateChannel, contentResolver)
                            }
                            Constants.MODE_TYPE_STOP -> {
                                recSocket.close()
                                serverSocket.close()
                                throw Exception("Stopped by sender")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception in start: $e")
            }
        }
        return updateChannel.receiveAsFlow()
    }

    fun stop() {
        ioScope.coroutineContext.cancel()
        recSocket.close()
        serverSocket.close()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun receiveFileOverTCP(recSocket: Socket, updateChannel: Channel<String>, contentResolver: ContentResolver) = coroutineScope {
        // send continue signal or ignore signal
        val outStream = recSocket.getOutputStream()
        outStream.write(Constants.TCP_FILE_CONTINUE.toByteArray())
        val inStream = recSocket.getInputStream()
        while (inStream.available() == 0 && isActive) {
            delay(100)
        }
        val readInfoBuff = ByteArray(inStream.available())
        val read = inStream.read(readInfoBuff)
        val reply = String(readInfoBuff, 0, read)
        if(!reply.contains(Constants.TCP_FILE_CONTINUE)) {
            Log.d(tag, "TCP file continue signal not received")
            updateChannel.send("TCP file continue signal not received")
            return@coroutineScope
        }
        val fileInfoTriple = try {
            Converters.extractTripleData(reply)
        } catch (e: Exception) {
            Log.e(tag, "error extracting file info")
            updateChannel.send("error extracting file info")
            return@coroutineScope
        }
        val fileName = fileInfoTriple.first
        val type = fileInfoTriple.second
        val fileSize = fileInfoTriple.third
        Log.d(tag, "TCP file info : $fileName size >>> $fileSize")
        updateChannel.send("TCP file info : $fileName size >>> $fileSize")
        // start file write process
        try {
            val newFileUri = getNewFileUri(fileName, type, contentResolver)
            val fileOut = contentResolver.openOutputStream(newFileUri!!)
            outStream.write(Constants.TCP_FILE_CONTINUE.toByteArray())
            updateChannel.send("ready to receive: $fileName")
            Log.d(tag, "ready to receive: opened file out stream")
            var totalReceived = 0L
            var loops = 0
            do {
                loops++
                val buff = ByteArray(4096)
                while (inStream.available() == 0) {
                    delay(100)
                }
                val read1 = inStream.read(buff)
                fileOut?.write(buff, 0, read1)
                totalReceived += read1
                if (loops % 100 == 0) {
                    updateChannel.send("Received: $totalReceived Bytes")
                    Log.d(tag, "Received: $totalReceived Bytes")
                }
            } while (totalReceived < fileSize.toLong())
            updateChannel.send("Finished: $totalReceived Bytes")
            Log.d(tag, "Finished: $totalReceived Bytes")
        } catch (e:Exception) {
            updateChannel.send("An Error occurred while writing file: $e")
            Log.e(tag, "error $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getNewFileUri(filename:String, type: String, contentResolver: ContentResolver) : Uri? {

        val itemUri:Uri?
        when(type) {
            Constants.FILE_TYPE_IMAGE -> {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            Constants.FILE_TYPE_VIDEO -> {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/*")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            Constants.FILE_TYPE_AUDIO -> {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            else -> {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "*/*")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
        }
        return itemUri
    }
}