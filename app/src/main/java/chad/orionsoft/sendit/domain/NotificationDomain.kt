package chad.orionsoft.sendit.domain

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import chad.orionsoft.sendit.MainActivity
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
    private val silentChannelId = "Send_It_Silent_Id"
    private val silentChannelName = "Send_it_silent_channel"
    private var nManager : NotificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private val defaultPendingIntent : PendingIntent

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            createSilentNotificationChannel()
        }
        val defaultActIntent = Intent(context, MainActivity::class.java)
        defaultPendingIntent = PendingIntent.getActivity(context, 100, defaultActIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("NotificationPermission")
    fun showNotification(
        nId: Int,
        title: String,
        content: String,
        isSticky: Boolean = false,
        progress: Int? = null,
        type: NotificationType = NotificationType.Silent
    ) : Notification {
        val notificationBuilder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (type) {
                NotificationType.Normal -> {
                    Notification.Builder(context, channelId)
                }
                NotificationType.Silent -> {
                    Notification.Builder(context, silentChannelId)
                }
                NotificationType.Floating -> TODO()
            }

        } else {
            Notification.Builder(context)
        }
        notificationBuilder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(isSticky)
            .setContentIntent(defaultPendingIntent)
        if(type == NotificationType.Silent) {
            notificationBuilder.setSound(null)
        }
        progress?.let {
            if(it > 0) {
                notificationBuilder.setProgress(100, it, false)
            } else {
                notificationBuilder.setProgress(100, 0, true)
            }
        }
        val notification = notificationBuilder.build()
        nManager.notify(
            nId, notification
        )
        return notification
    }

    fun closeNotification(notificationId: Int) {
        nManager.cancel(notificationId)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        nManager.createNotificationChannel(
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSilentNotificationChannel() {
        nManager.createNotificationChannel(
            NotificationChannel(silentChannelId, silentChannelName, NotificationManager.IMPORTANCE_LOW)
        )
    }

    enum class NotificationType {
        Normal, Silent, Floating
    }
}