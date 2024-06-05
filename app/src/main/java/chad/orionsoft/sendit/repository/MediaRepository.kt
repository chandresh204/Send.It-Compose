package chad.orionsoft.sendit.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import chad.orionsoft.sendit.model.MediaImageData
import chad.orionsoft.sendit.model.MediaImageDataQ
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val imagesList = mutableListOf<MediaImageData>()
    private val imageListQ = mutableListOf<MediaImageDataQ>()

    fun getImages() : List<MediaImageData> {
        val imageURI= MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.query(
            imageURI,null,null,null,null).use { cursor ->
            if(cursor!=null && cursor.moveToFirst()) {

                var count=0
                val indexID=cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val indexDATa=cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val titleIndex=cursor.getColumnIndex(MediaStore.Images.Media.TITLE)

                do {
                    val thisID=cursor.getLong(indexID)
                    val data=cursor.getString(indexDATa)
                    val title=cursor.getString(titleIndex)
                    count++
                    imagesList.add(MediaImageData(thisID,data,title, ""))
                } while (cursor.moveToNext())
            }
        }
        imagesList.sortByDescending { imageObject -> imageObject.lastModified }
        return imagesList
    }

    fun getImagesQ() : List<MediaImageDataQ> {
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        context.contentResolver
            .query(imageUri, null, null, null, null).use { iCursor ->
                if(iCursor!=null && iCursor.moveToFirst()) {
                    val idIndex = iCursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val titleIndex = iCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeIndex = iCursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val modifyIndex = iCursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)

                    do {
                        val id = iCursor.getLong(idIndex)
                        val uri = ContentUris.withAppendedId(imageUri, id)
                        val title = iCursor.getString(titleIndex)
                        var size = iCursor.getLong(sizeIndex)
                        val modified = iCursor.getLong(modifyIndex)
                        if (size == 0L) {
                            val iStream = context.contentResolver.openInputStream(uri)
                            size = iStream?.available()?.toLong() ?: 0L
                            iStream?.close()
                        }
                        imageListQ.add(MediaImageDataQ(uri, title, size, modified))
                    } while (iCursor.moveToNext())
                }
            }
        imageListQ.sortByDescending { imageObjectQ -> imageObjectQ.lastModified }
        return imageListQ
    }
}