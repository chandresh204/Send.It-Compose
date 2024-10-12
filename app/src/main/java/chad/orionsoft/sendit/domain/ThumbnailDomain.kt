package chad.orionsoft.sendit.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ThumbnailDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Generates Thumbnail and stores in thumb file, return [Bitmap], only to use with SDK >= Q
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun generateThumbnail(uri: Uri) : Bitmap? {
        val contentResolver = context.contentResolver
        return try {
            contentResolver.loadThumbnail(uri, Size(300, 300), null)
        } catch (e: Exception) {
            Log.e("ThumbnailGenerateQ", "Error : $e")
            Log.d("ThumbnailGenerateQ", "Trying with mediaMetadataRetriever")
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                return bitmap
            } catch (e: Exception) {
                Log.e("ThumbnailGenerateQ", "MediaMetadataRetriever failed : $e")
            } finally {
                retriever.release()
            }
            return null
        }
    }

    /**
     * Generates Thumbnail and stores in thumb file, return [Bitmap], only to use with SDK < Q
     */
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
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(pathData, options)
                        options.inSampleSize = calculateInSampleSize(options, 300, 300)
                        options.inJustDecodeBounds = false
                        val mainImage = BitmapFactory.decodeFile(pathData, options)
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

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}