package chad.orionsoft.sendit.domain

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentResolverDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getNewFileUri(
        filename:String, type: FileType,
        contentResolver: ContentResolver = context.contentResolver
    ) : Uri? {
        val itemUri: Uri?
        when(type) {
            FileType.FileTypeImage -> {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            FileType.FileTypeVideo -> {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/*")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            FileType.FileTypeAudio -> {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
            else -> {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "*/*")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                itemUri = contentResolver.insert(collection, values)
            }
        }
        return itemUri
    }

    fun updateValueInContentResolver(
        fileType: FileType,
        newFileUri: Uri,
        contentResolver: ContentResolver = context.contentResolver
    ) {
        Log.d("ContentResolver", "update: $fileType, $newFileUri")
        when(fileType) {
            FileType.FileTypeImage -> {
                val newValue = ContentValues()
                newValue.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(newFileUri, newValue, null, null)
            }
            FileType.FileTypeVideo -> {
                val newValue = ContentValues()
                newValue.put(MediaStore.Video.Media.IS_PENDING, 0)
                contentResolver.update(newFileUri, newValue, null, null)
            }
            FileType.FileTypeAudio -> {
                val newValue = ContentValues()
                newValue.put(MediaStore.Audio.Media.IS_PENDING, 0)
                contentResolver.update(newFileUri, newValue, null ,null)
            }
            FileType.FileTypeOther -> {
                val newValue = ContentValues()
                newValue.put(MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(newFileUri, newValue, null ,null)
            }
        }
    }

    fun processFileSelectionIntent(intent: Intent,
                                   resolver: ContentResolver = context.contentResolver) : List<MediaData> {
        val mediaList = mutableListOf<MediaData>()
        intent.clipData?.let {
            for(i in 0 until it.itemCount) {
                val clipItem = it.getItemAt(i)
                val uri = clipItem.uri
                resolver.query(uri, null, null, null,null)
                    .use {  cur ->
                        cur?.let {  cur1 ->
                            createMediaDataFromResolverCursor(cur1, uri)?.let { mediaData ->
                                mediaList.add(mediaData)
                            }
                        }
                    }

            }
        }
        val dataUri = intent.data ?: Uri.parse("")
        resolver.query(dataUri, null, null, null, null).use {
            it?.let { cur ->
                createMediaDataFromResolverCursor(cur, dataUri)?.let { mediaData -> mediaList.add(mediaData) }
            }
        }
        return mediaList
    }

    private fun createMediaDataFromResolverCursor(cur1: Cursor, uri: Uri) : MediaData? {
        if(cur1.moveToFirst()) {
            val titleIndex = cur1.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeIndex = cur1.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val id = System.currentTimeMillis()
            val title = cur1.getString(titleIndex)
            val size = cur1.getLong(sizeIndex)
            return MediaData(id, uri, title, null, -1, uri, size,
                    -1, Converters.getFileTypeFromFileName(title)
            )
        }
        return null
    }
}