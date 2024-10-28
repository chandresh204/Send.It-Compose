package chad.orionsoft.sendit.work_manager

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.model.TransferringMediaItem
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.receiveData
import chad.orionsoft.sendit.utils.sendData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.Socket

@HiltWorker
class TcpSenderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val notificationDomain: NotificationDomain,
    private val connectProcessRepository: ConnectProcessRepository
) : CoroutineWorker(context, params) {

    private val notificationId = Constants.SENDER_WM_NOTIFICATION_ID
    private val tag = "TCPSenderWorker"
    private var foregroundInfo: ForegroundInfo

    init {
        val notification =  notificationDomain.showNotification(
            notificationId, "Sending", ""
        )
        foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        setForegroundAsync(foregroundInfo)
        return startFileSend()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return foregroundInfo
    }

    private suspend fun startFileSend() : Result  {
        return withContext(Dispatchers.IO) {
            val connectedDevice = connectProcessRepository.getPairingDeviceDetails()
            if (connectedDevice == null) {
                showErrorNotification("Device disconnected")
                return@withContext Result.success()
            }
            val selectedItemsToSend = TransferProcessDomain.getTheCurrentTransferSendingList()
            if(selectedItemsToSend.isEmpty()) {
                Log.d(tag, "Nothing selected, stopping TCPSender")
                return@withContext Result.success()
            }
            val totalSizeToSend = selectedItemsToSend.fold(0L) { acc, mData ->
                acc.plus(
                    mData.size
                )
            }
            val modeStr = "${Constants.TCP_MODE_STR}((${Constants.MODE_TYPE_FILE}))${selectedItemsToSend.size}" +
                    "/*$totalSizeToSend*/"
            Log.d(tag, "s1 >>> mode str sending $modeStr to ${connectedDevice.address} : ${connectedDevice.serverPort}")
            val tcpSocket =try {
                Socket(connectedDevice.address, connectedDevice.serverPort)
            } catch (e: Exception) {
                connectProcessRepository.markAsDisconnected()
                TransferProcessDomain.clearSession()
                Log.e(tag, "could not create socket, probably device disconnected")
                return@withContext Result.success()
            }
            val reply = tcpSocket.sendData(modeStr)
            Log.d(tag, "s1_1 >>> mode str sent")
            if(reply.success && reply.resMsg?.contains(Constants.TCP_CONTINUE) == true) {
                Log.d(tag, "s2 >>> entering in loop to send items")
                TransferProcessDomain.totalSentCount
                var loopCount = 0
                while (true) {
                    TransferProcessDomain.isSending = true
                    val file = try {
                        selectedItemsToSend[loopCount]
                    } catch (e:IndexOutOfBoundsException) {
                        break
                    }
                    val totalCount = selectedItemsToSend.size
                    val fileInfo = Converters.createTripleData(Constants.TCP_FILE_INFO, file.title, file.mediaType.title, file.size)
                    Log.d(tag, "s3 >>> sending file info for: ${file.title}")
                    notificationDomain.showNotification(
                        notificationId, "Sending - ${file.title}", "${TransferProcessDomain.totalSentCount + 1}/$totalCount", false,
                        progress = (TransferProcessDomain.totalSentCount+1)*100/totalCount
                    )
                    TransferProcessDomain.updateSendingItemCount(TransferProcessDomain.totalSentCount+1)
                    TransferProcessDomain.emitTitleOfCurrentSendingItem(file.title)
                    val reply2 = tcpSocket.sendData(fileInfo)
                    if(reply2.success && reply2.resMsg?.contains(Constants.TCP_CONTINUE) == true) {
                        // start sending file
                        Log.d(tag, "s5 >>> sending file over tcp ${file.title}")
                        tcpSocket.soTimeout = 10000
                        if(startSendingFile(tcpSocket.getOutputStream(), file)) {
                            TransferProcessDomain.totalSentCount++
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
                    loopCount++
                }
            }
            tcpSocket.sendData(Constants.TCP_DONE, waitForResponse = false)
            Log.d(tag, "s8 >>> disconnect signal sent")
            TransferProcessDomain.clearQueueAndMoveToFinish()
            tcpSocket.close()
            TransferProcessDomain.isSending = false
            return@withContext Result.success()
        }
    }

    private suspend fun startSendingFile(outStream: OutputStream, fileData: TransferringMediaItem) : Boolean {
        return withContext(Dispatchers.IO) {
            var totalSent = 0L
            var loops = 0
            val fileInputStream = context.contentResolver.openInputStream(fileData.uri ?: Uri.parse(""))
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
                    TransferProcessDomain.addToTotalSentInSession(read)
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
            Constants.SENDER_WM_ERR_NOTIFICATION_ID, "Sending Failed", text, false
        )
    }
}