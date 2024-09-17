package chad.orionsoft.sendit.domain

import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
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

    fun provideFile(filename:String) : File {
        val appDir= File(Environment.getExternalStorageDirectory(),"Send_it")
        val saveDir= File(Environment.getExternalStorageDirectory(), "send_it")
        if(!appDir.exists())
            appDir.mkdirs()
        var exists=false
        val fList = appDir.list() ?: emptyArray()
        for(i in fList.indices) {
            if(filename==fList[i])
                exists=true
        }
        return if(exists) {
            val lastIndexOfName = filename.lastIndexOf(".")
            val file1= filename.substring(0,lastIndexOfName)
            val ext = filename.substring(lastIndexOfName)
            File(saveDir,"$file1(1)$ext")
            provideFile("$file1(1)$ext")
        } else {
            File(saveDir,filename)
        }
    }

    fun deleteFile(uri: Uri?, context: Context) : Boolean {
        if(uri == null) return false
        // TODO delete file
        Log.e("Delete file", "Operation not implemented")
        return false
    }
}