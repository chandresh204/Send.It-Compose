package chad.orionsoft.sendit.work_manager

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.repository.ConnectionRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket

class TcpServerWorker(val context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params) {

    private var notificationDomain: NotificationDomain = NotificationDomain(context)
    private lateinit var serverSocket: ServerSocket
    private lateinit var recSocket : Socket
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val tag = "TcpServerWorker"
    private val onPort = params.inputData.getInt(Constants.TCP_SERVER_WM_PORT, 4848)
    private val connectionName = params.inputData.getString(Constants.TCP_SERVER_WM_DEVICE)
    private val _updateChannel = Channel<String>()
    val updateFlow = _updateChannel.receiveAsFlow()
    private val notificationId = 25

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        println("TCPServerWorker >>> Running")
        val connectionText = "Started on $onPort, Connected to: $connectionName"
        val notification = notificationDomain.showNotification(
            notificationId, "TcpServer",
            connectionText
        )
        setForegroundAsync(ForegroundInfo(notificationId, notification,
            FOREGROUND_SERVICE_TYPE_DATA_SYNC))
        /*for (i in 0 until 20) {
            delay(1000)
            notificationDomain.showNotification(
                10, "TCPServer $i",
                "Started on $onPort, Connected to: $connectionName"
            )
        }
        return Result.success()*/
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Starting tcp server on port: $onPort")
                serverSocket = ServerSocket(onPort)
                serverSocket.soTimeout = 60000
                recSocket = serverSocket.accept()
                Log.d(tag, "Receiving on tcp")
                val inStream = recSocket.getInputStream()
                val outStream = recSocket.getOutputStream()
                while (true) {
                    while (inStream.available() == 0) {
                        delay(100)
                    }
                    val buff = ByteArray(inStream.available())
                    val read = inStream.read(buff)
                    val msg = String(buff, 0, read)
                    Log.d(tag, "Received: $msg")
                    notificationDomain.showNotification(
                        notificationId, "TCP Server",
                        "$connectionText - received: $msg"
                    )
                    _updateChannel.send("Received: $msg")
                    if (msg.contains(Constants.TCP_MODE_STR)) {
                        val modeStr = Converters.extractDataInNormalCase(msg)
                        when (modeStr) {
                            Constants.MODE_TYPE_FILE -> {
                                Log.d(tag, "File will be received here")
                                _updateChannel.send("waiting for file to receive")
                                receiveFileOverTCP(recSocket, _updateChannel)
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
                showErrorNotification("Error: $e")
                ConnectionRepository.updateConnectionInfo(null)
                return@withContext Result.failure()
            }
        }
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun receiveFileOverTCP(recSocket: Socket, updateChannel: Channel<String>) = coroutineScope {
        val contentResolver = context.contentResolver
        // send continue signal or ignore signal
        val outStream = recSocket.getOutputStream()
        outStream.write(Constants.TCP_FILE_CONTINUE.toByteArray())
        val inStream = recSocket.getInputStream()
        while (inStream.available() == 0) {
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
            showErrorNotification("Error Writing File : $e")
            Log.e(tag, "error $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getNewFileUri(filename:String, type: String, contentResolver: ContentResolver) : Uri? {

        val itemUri: Uri?
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

    private suspend fun showErrorNotification(text: String) {
        delay(3000)
        notificationDomain.showNotification(
            notificationId +1, "TCP Server Stopped", text, false
        )
    }
}