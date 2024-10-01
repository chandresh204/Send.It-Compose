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
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    private val tag = "TcpServerWorker"
    private val onPort = params.inputData.getInt(Constants.TCP_SERVER_WM_PORT, 4848)
    private val connectionName = params.inputData.getString(Constants.TCP_SERVER_WM_DEVICE)
    private val notificationId = 25

    private lateinit var foregroundInfo: ForegroundInfo

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return foregroundInfo
    }

    companion object {
        var isTCPServerRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        Log.d(tag, ">>> Starting")
        if(ConnectProcessRepository.getPairingDeviceDetails() == null) {
            Log.d(tag, ">>> Stopping due to not connected")
            showErrorNotification("TCPServer : Not connected to any device")
            return Result.failure()
        }
        val connectionText = "Started on $onPort, Connected to: $connectionName"
        val notification = notificationDomain.showNotification(
            notificationId, "TcpServer",
            connectionText
        )
        foregroundInfo = ForegroundInfo(notificationId, notification,
            FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        setForegroundAsync(foregroundInfo)
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Starting tcp server on port: $onPort")
                serverSocket = ServerSocket(onPort)
                while (true) {
                    isTCPServerRunning = true
                    serverSocket.soTimeout = 300000
                    recSocket = serverSocket.accept()
                    Log.d(tag, "r1 >>> Receiving on tcp")
                    val inStream = recSocket.getInputStream()
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
                    if (msg.contains(Constants.TCP_MODE_STR)) {
                        val modeStr = Converters.extractTripleData(msg)
                        when (modeStr.first) {
                            Constants.MODE_TYPE_FILE -> {
                                Log.d(tag, "r3 >>> File will be received here")
                                receiveFileOverTCP(recSocket)
                                Log.d(tag, "r7 >>> All file received")
                                recSocket.close()
                            }

                            Constants.MODE_TYPE_DISCONNECT -> {
                                recSocket.close()
                       //         serverSocket.close()
                                throw Exception("Stopped by sender")
                            }
                        }
                    } else {
                        Log.d(tag, "$msg does not contain any info")
                    }
                }
            } catch (e: Exception) {
                isTCPServerRunning = false
                Log.e(tag, "Exception in start: $e")
                showErrorNotification("Error: $e")
                ConnectProcessRepository.updateAndEmitConnectedDeviceDetails(null)
                return@withContext Result.failure()
            }
        }
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun receiveFileOverTCP(recSocket: Socket) = coroutineScope {
        notificationDomain.showNotification(
            notificationId, "Preparing", "----", false , progress = -1
        )
        // send continue signal or ignore signal
        val outStream = recSocket.getOutputStream()
        outStream.write(Constants.TCP_CONTINUE.toByteArray())
        val inStream = recSocket.getInputStream()

        // enter loop to receive files
        var success = 0
        var count = 0
        while (true) {
            while (inStream.available() == 0) {
                delay(100)
            }
            val readInfoBuff = ByteArray(inStream.available())
            val read = inStream.read(readInfoBuff)
            val msg = String(readInfoBuff, 0, read)

            if(msg.contains(Constants.TCP_DONE)) {
                Log.d(tag, "r4 >>> received completed")
                break
            }

            if(msg.contains(Constants.TCP_FILE_INFO)) {
                Log.d(tag, "r4 >>> received file info: $msg")
                val fileInfo = Converters.extractTripleData(msg)
                val filename = fileInfo.first
                val type = Converters.convertToFileType(fileInfo.second)
                val size = fileInfo.third
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
            count++
        }
        showErrorNotification(
            "Received $success/$count succeed"
        )
        inStream.close()
        return@coroutineScope
    }

    private suspend fun startReceivingFile(
        inStream: InputStream,
        filename: String,
        type: FileType,
        size: Long
    ) : Boolean {
        // start file write process
        return withContext(Dispatchers.IO) {
            val fileId = TransferProcessDomain.addToReceivingList(filename, size)
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
                    TransferProcessDomain.emitTitleOfCurrentReceivingItem(filename)
                    TransferProcessDomain.addToTotalReceivedInSession(read1)
                    if (loops % Constants.UPDATE_TRANSFER_STATUS_AFTER_LOOPS == 0) {
                        TransferProcessDomain.updateItemReceivedProgress(fileId, totalReceived)
                        notificationDomain.showNotification(
                            notificationId, "Receiving", filename, false ,
                            progress = (totalReceived*100/size).toInt(),
                            type = NotificationDomain.NotificationType.Silent
                        )
                        Log.d(tag, "Received: $totalReceived Bytes")
                    }
                } while (totalReceived < size)
                fileOut?.flush()
                fileOut?.close()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // update contentResolver
                    newFileUri?.let {
                        contentResolverDomain.updateValueInContentResolver(
                            type, it
                        )
                        TransferProcessDomain.itsTimeToUpdateUri(
                            it, fileId
                        )
                    }
                } else {
                    // TODO need to update the thumbnail directly from file
                }
                Log.d(tag, "r6 >>> Finished: $totalReceived Bytes")
                TransferProcessDomain.updateItemTransferStatus(
                    fileId, ProgressStatus.Success, false
                )
                return@withContext true
            } catch (e: Exception) {
                showErrorNotification("Error Writing File : $e")
                Log.e(tag, "r6 >>> error $e")
                TransferProcessDomain.updateItemTransferStatus(
                    fileId, ProgressStatus.Failed, false
                )
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