package chad.orionsoft.sendit.domain

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import chad.orionsoft.sendit.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val channelId = "Send_It_Channel_Id"
    private val channelName = "Send_it_channel"
    private var nManager : NotificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @SuppressLint("NotificationPermission")
    fun showNotification(
        nId: Int,
        title: String,
        content: String,
    ) : Notification {
        val notificationBuilder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }
        val notification = notificationBuilder
            .setSmallIcon(R.drawable.send_icon1_small)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .build()
        nManager.notify(
            nId, notification

        )
        return notification
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        nManager.createNotificationChannel(
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        )
    }
}