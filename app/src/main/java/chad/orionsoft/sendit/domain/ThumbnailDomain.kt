package chad.orionsoft.sendit.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ThumbnailDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Suppress("DEPRECATION")
    fun generateThumbnail(thumbName:String, pathData: String, fileType: FileType) : Bitmap? {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            throw Exception("UnnecessaryThumbnailGeneration")
        }
        val appDir = context.getExternalFilesDir(Constants.THUMBNAIL_DIR)
        if(appDir?.exists() != true)
            appDir?.mkdirs()
        var returnMap : Bitmap? = null
        if(thumbName.isNotEmpty()) {
            val thumbFile = File(appDir, "$thumbName.jpg")
            if(!thumbFile.exists()) {
                when(fileType) {
                    FileType.FileTypeImage, FileType.FileTypeAudio -> {
                        val mainImage = BitmapFactory.decodeFile(pathData)
                        if(mainImage != null) {
                            val asRation=mainImage.width.toFloat()/mainImage.height.toFloat()
                            val newHeight=300/asRation
                            val bMap = Bitmap.createScaledBitmap(mainImage, 300, newHeight.toInt(), false)
                            bMap.compress(Bitmap.CompressFormat.JPEG, 90, thumbFile.outputStream())
                        }
                    }
                    FileType.FileTypeVideo -> {
                        val bMap = ThumbnailUtils.createVideoThumbnail(pathData, MediaStore.Video.Thumbnails.MICRO_KIND)
                        bMap?.compress(Bitmap.CompressFormat.JPEG, 90, thumbFile.outputStream())
                    }
                    FileType.FileTypeOther -> {}
                }

            }
            returnMap = BitmapFactory.decodeFile(thumbFile.absolutePath)
        }
        return returnMap
    }
}