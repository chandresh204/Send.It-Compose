package chad.orionsoft.sendit.work_manager

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import chad.orionsoft.sendit.domain.ContentResolverDomain
import chad.orionsoft.sendit.domain.FileIOOperations.provideFile
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.model.FileType
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
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

class TcpServerWorker(val context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params) {

    private val notificationDomain: NotificationDomain = NotificationDomain(context)
    private val contentResolverDomain : ContentResolverDomain = ContentResolverDomain(context)
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
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Starting tcp server on port: $onPort")
                serverSocket = ServerSocket(onPort)
                serverSocket.soTimeout = 300000
                recSocket = serverSocket.accept()
                Log.d(tag, "r1 >>> Receiving on tcp")
                val inStream = recSocket.getInputStream()
                val outStream = recSocket.getOutputStream()
                while (true) {
                    notificationDomain.showNotification(
                        notificationId, "TCP Server",
                        connectionText
                    )
                    while (inStream.available() == 0) {
                        delay(100)
                    }
                    val buff = ByteArray(inStream.available())
                    val read = inStream.read(buff)
                    val msg = String(buff, 0, read)
                    Log.d(tag, "r2 >>> Received mode str: $msg")
                    notificationDomain.showNotification(
                        notificationId, "TCP Server",
                        "$connectionText - received: $msg"
                    )
//                    _updateChannel.send("Received: $msg")
                    if (msg.contains(Constants.TCP_MODE_STR)) {
                        val modeStr = Converters.extractTripleData(msg)
                        when (modeStr.first) {
                            Constants.MODE_TYPE_FILE -> {
                                Log.d(tag, "File will be received here")
//                                _updateChannel.send("waiting for file to receive")
                                receiveFileOverTCP(recSocket, modeStr.second.toInt(),
                                    modeStr.second.toLong(),_updateChannel)
                            }

                            Constants.MODE_TYPE_DISCONNECT -> {
                                recSocket.close()
                                serverSocket.close()
                                throw Exception("Stopped by sender")
                            }
                        }
                    } else {
                        Log.d(tag, "$msg does not contain any info")
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
    private suspend fun receiveFileOverTCP(recSocket: Socket, count: Int,
                                           totalSize: Long,
                                           updateChannel: Channel<String>) = coroutineScope {
        notificationDomain.showNotification(
            notificationId, "Preparing", "0/$count", false , progress = -1
        )
        // send continue signal or ignore signal
        val outStream = recSocket.getOutputStream()
        outStream.write(Constants.TCP_CONTINUE.toByteArray())
        val inStream = recSocket.getInputStream()

        // enter loop to receive files
        var success = 0
        for (i in 0 until count) {

            while (inStream.available() == 0) {
                delay(100)
            }
            val readInfoBuff = ByteArray(inStream.available())
            val read = inStream.read(readInfoBuff)
            val msg = String(readInfoBuff, 0, read)

            if(msg.contains(Constants.TCP_STOP)) {
                Log.d(tag, "r4 >>> received stop")
                break
            }

            if(msg.contains(Constants.TCP_FILE_INFO)) {
                Log.d(tag, "r4 >>> received file info: $msg")
                val fileInfo = Converters.extractTripleData(msg)
                val filename = fileInfo.first
                val type = Converters.convertToFileType(fileInfo.second)
                val size = fileInfo.third
                notificationDomain.showNotification(
                    notificationId, filename, "${i + 1}/$count", false , progress = (i+1)*100/count
                )
                // send continue signal
                outStream.write(Constants.TCP_CONTINUE.toByteArray())
                // start receiving file
                Log.d(tag, "r5 >>> start file receiving")
                if(startReceivingFile(inStream, filename, type, size.toLong())) {
                    success++
                }
                // send continue signal
                outStream.write(Constants.TCP_CONTINUE.toByteArray())
            }

        }
        showErrorNotification(
            "Received $success/$count succeed"
        )

        return@coroutineScope

       /* if(!reply.contains(Constants.TCP_CONTINUE)) {
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
            outStream.write(Constants.TCP_CONTINUE.toByteArray())
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
        }*/
    }

    private suspend fun startReceivingFile(
        inStream: InputStream,
        filename: String,
        type: FileType,
        size: Long
    ) : Boolean {
        // start file write process
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                var newFileUri: Uri? = null
                val fileOut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    newFileUri = contentResolverDomain.getNewFileUri(filename, type)
                    contentResolver.openOutputStream(newFileUri!!)
                } else {
                    provideFile(filename).outputStream()
                }
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
                    if (loops % 1000 == 0) {
                        Log.d(tag, "Received: $totalReceived Bytes")
                    }
                } while (totalReceived < size)
                fileOut?.flush()
                fileOut?.close()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // update contentResolver
                    newFileUri?.let {
                        contentResolverDomain.updateValueInContentResolver(
                            type, newFileUri
                        )
                    }
                }
                Log.d(tag, "Finished: $totalReceived Bytes")
                return@withContext true
            } catch (e: Exception) {
                showErrorNotification("Error Writing File : $e")
                Log.e(tag, "error $e")
                return@withContext false
            }
        }
    }

    private suspend fun showErrorNotification(text: String) {
        delay(3000)
        notificationDomain.showNotification(
            notificationId +1, "TCP Server Stopped", text, false
        )
    }
}