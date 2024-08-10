package chad.orionsoft.sendit.work_manager

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import chad.orionsoft.sendit.domain.NotificationDomain
import kotlinx.coroutines.delay

class TcpSenderWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val notificationId = 99
    private val notificationDomain = NotificationDomain(context)

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        val notification =  notificationDomain.showNotification(
            notificationId, "TCPSender", "Running"
        )
        setForegroundAsync(ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC))
        for (i in 0 until 20) {
            delay(1000)
        }
        return Result.success()
    }

}