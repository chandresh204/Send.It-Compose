package chad.orionsoft.sendit.work_manager

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.repository.ConnectionRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.receiveData
import chad.orionsoft.sendit.utils.sendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.Socket

class TcpSenderWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val notificationId = 99
    private val notificationDomain = NotificationDomain(context)
    private val tag = "TCPSenderWorker"

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        val notification =  notificationDomain.showNotification(
            notificationId, "TCPSender", "Sending"
        )
        setForegroundAsync(ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC))
        return startFileSend()
    }

    private suspend fun startFileSend() : Result  {
        return withContext(Dispatchers.IO) {
            val connectedDevice = ConnectionRepository.getConnectedDevice()
            if (connectedDevice == null) {
                showErrorNotification("Connected Device details not found")
                return@withContext Result.success()
            }
            val selectedItemsToSend = ConnectionRepository.getSelectedItems().toList()
            if(selectedItemsToSend.isEmpty()) {
                Log.d(tag, "Nothing selected, stopping TCPSender")
                return@withContext Result.success()
            }
            ConnectionRepository.clearAllSelection()
            val totalSizeToSend = selectedItemsToSend.fold(0L) { acc, mData ->
                acc.plus(
                    mData.size
                )
            }
            notificationDomain.showNotification(
                notificationId, "TCPSender", "Total ${selectedItemsToSend.size} items to send. " +
                        "Total ${Converters.formatDataString(totalSizeToSend)}"
            )
            val modeStr = "${Constants.TCP_MODE_STR}((${Constants.MODE_TYPE_FILE}))${selectedItemsToSend.size}" +
                    "/*$totalSizeToSend*/"
            Log.d(tag, "s1 >>> mode str sending $modeStr")
            val tcpSocket = Socket(connectedDevice.address, connectedDevice.serverPort)
            val reply = tcpSocket.sendData(modeStr)
            Log.d(tag, "s1_1 >>> mode str sent")
            if(reply.success && reply.resMsg?.contains(Constants.TCP_CONTINUE) == true) {
                val totalCount = selectedItemsToSend.size
                Log.d(tag, "s2 >>> entering in loop to send $totalCount items")
                var success = 0
                for ((count, file) in selectedItemsToSend.withIndex()) {
                    val fileInfo = Converters.createTripleData(Constants.TCP_FILE_INFO, file.title, file.mediaType.title, file.size)
                    Log.d(tag, "s3 >>> sending file info for: ${file.title}")
                    notificationDomain.showNotification(
                        notificationId, file.title, "${count + 1}/$totalCount", false,
                        progress = (count+1)*100/totalCount
                    )
                    TransferProcessDomain.updateSendingItemCount(count+1)
                    val reply2 = tcpSocket.sendData(fileInfo)
                    if(reply2.success && reply2.resMsg?.contains(Constants.TCP_CONTINUE) == true) {
                        // start sending file
                        Log.d(tag, "s5 >>> sending file over tcp ${file.title}")
                        if(startSendingFile(tcpSocket.getOutputStream(), file)) {
                            success++
                        }
                        // wait for continue signal
                        Log.d(tag, "s7 >>> wait for continue signal")
                        val resp1 = tcpSocket.receiveData()
                        Log.d(tag, "s7_1 >>> continue response received $resp1")
                        if(!resp1.success) {
                            Log.d(tag, "s7_2 >>> failed to get continue signal , stopping process")
                            break
                        }
                    }
                }
                showErrorNotification(
                    "Sent $success/$totalCount successfully"
                )
            }
            tcpSocket.close()
            return@withContext Result.success()
        }
    }

    private suspend fun startSendingFile(outStream: OutputStream, fileData: MediaData) : Boolean {
        return withContext(Dispatchers.IO) {
            var totalSent = 0L
            var loops = 0
            val fileInputStream = context.contentResolver.openInputStream(fileData.uri)
            if(fileInputStream == null) {
                Log.e(tag, "${fileData.title} >>> input stream is null")
                return@withContext false
            }
            try {
                TransferProcessDomain.updateItemTransferStatus(fileData.id, ProgressStatus.Sending, true)
                do {
                    loops++
                    val buff = ByteArray(4096)
                    val read = fileInputStream.read(buff)
                    outStream.write(buff, 0, read)
                    totalSent += read
                    if(loops % Constants.UPDATE_TRANSFER_STATUS_AFTER_LOOPS == 0) {
                        TransferProcessDomain.updateItemSentProgress(fileData.id, totalSent)
                        Log.d(tag, "Sending update : Sending ${fileData.id} -> $totalSent")
                    }
                } while (totalSent < fileData.size)
                Log.d(tag, "${fileData.title} >>> $totalSent Bytes Success")
                fileInputStream.close()
                TransferProcessDomain.updateItemTransferStatus(fileData.id, ProgressStatus.Success, true)
                return@withContext true
            } catch (e:Exception) {
                Log.e(tag, "${fileData.title} >>> $totalSent Bytes Failed >>> $e")
                TransferProcessDomain.updateItemTransferStatus(fileData.id, ProgressStatus.Failed, true)
                return@withContext false
            }
        }
    }

    private suspend fun showErrorNotification(text: String) {
        delay(3000)
        notificationDomain.showNotification(
            notificationId +1, "Sender Stopped", text, false
        )
    }
}