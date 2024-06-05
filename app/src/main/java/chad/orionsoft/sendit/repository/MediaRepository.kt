package chad.orionsoft.sendit.repository

import android.content.Context
import android.provider.MediaStore
import chad.orionsoft.sendit.model.MediaImageData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val imagesList = mutableListOf<MediaImageData>()
    private fun getImages() : List<MediaImageData> {
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
}