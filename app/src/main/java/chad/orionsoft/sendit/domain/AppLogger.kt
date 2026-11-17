package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import chad.orionsoft.sendit.BuildConfig
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

private const val TAG = "AppLogger"

class AppLogger @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private var logFile : File
    init {
        Log.d(TAG, "Initialized AppLogger")
        logFile = File(context.filesDir, "log.txt")
        if(!logFile.exists()) {
            logFile.createNewFile()
            logFile.appendText("App Version: ${BuildConfig.VERSION_NAME}")
            logFile.appendText("\nDevice : ${Build.MANUFACTURER} ${Build.MODEL}}")
            logFile.appendText("\nAndroid : ${Build.VERSION.SDK_INT}")
        }
    }

    fun log(message: String, type: LogType = LogType.Verbose) {
        val printMsg = "${type.alias} $message"
        Log.d(TAG, printMsg)
        logFile.appendText("\n${Converters.formatDateTime(System.currentTimeMillis())} - $printMsg")
    }

    fun sendLogFile() {
        try {
            val attachmentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("u4chandresh@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "mTransmitter Logs")
                putExtra(Intent.EXTRA_STREAM, attachmentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(emailIntent, "Send logs using Mail....").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            log("Error sending log file", LogType.Error)
            Log.e(TAG, "Error sending log file", e)
        }
    }
}

enum class LogType(val alias: String) {
    Error("ERR."),
    Verbose(""),
    Warning("WAR.")
}