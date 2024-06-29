package chad.orionsoft.sendit.domain

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File

object FileIOOperations {

    fun getMyFreeSpace(context: Context) : Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val stManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            stManager.getFreeBytes(StorageManager.UUID_DEFAULT)
        } else {
            val file = File(Environment.getExternalStorageDirectory(),"Send_it")
            file.freeSpace
        }
    }
}