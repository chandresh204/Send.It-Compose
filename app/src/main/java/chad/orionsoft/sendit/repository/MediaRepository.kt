package chad.orionsoft.sendit.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val imagesList = mutableListOf<MediaData>()
    private val audioList = mutableListOf<MediaData>()
    private val videosList = mutableListOf<MediaData>()

    fun getImages() : List<MediaData> {
        imagesList.clear()
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        context.contentResolver
            .query(imageUri, null, null, null, null).use { iCursor ->
                if(iCursor!=null && iCursor.moveToFirst()) {
                    val idIndex = iCursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val titleIndex = iCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeIndex = iCursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val modifyIndex = iCursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                    val mediaDataPath = iCursor.getColumnIndex(MediaStore.Images.Media.DATA) // only for SDK < Q

                    do {
                        val id = iCursor.getLong(idIndex)
                        val uri = ContentUris.withAppendedId(imageUri, id)
                        val title = iCursor.getString(titleIndex)
                        var size = iCursor.getLong(sizeIndex)
                        val modified = iCursor.getLong(modifyIndex)
                        val pathData = iCursor.getString(mediaDataPath)
                        try {
                            if (size == 0L) {
                                val iStream = context.contentResolver.openInputStream(uri)
                                size = iStream?.available()?.toLong() ?: 0L
                                iStream?.close()
                            }
                        } catch (e: Exception) {
                            Log.e("getImages", "exception on title: $title >> $e")
                        }
                        imagesList.add(MediaData(id, uri, title, null, -1, uri, size, modified,
                            FileType.FileTypeImage, pathData = pathData))
                    } while (iCursor.moveToNext())
                }
            }
        imagesList.sortByDescending { imageObjectQ -> imageObjectQ.lastModified }
        return imagesList
    }

    fun generateAudioList() : List<MediaData> {
        audioList.clear()
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        val aCursor = context.contentResolver.query(audioUri,null,null,null,null)
        val alCursor = context.contentResolver.query(albumUri,null,null,null,null)

        if (aCursor!=null && aCursor.moveToFirst()) {

            val idIndex = aCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val alIndex1 = aCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val artistIndex = aCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val durationIndex = aCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val sizeIndex = aCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val nameIndex = aCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val modifiedIndex = aCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
            val albumArtDataIndex = alCursor?.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART) // Required for sdk < Q

            do {
                val alId1 = aCursor.getLong(alIndex1)
                var artUri = Uri.parse("")
                var matchedAlbumArtPosition : Int? = null
                if (alCursor!=null && alCursor.moveToFirst()) {

                    val alIndex2 = alCursor.getColumnIndex(MediaStore.Audio.Albums._ID)
                    do {
                        val alId2 = alCursor.getLong(alIndex2)
                        if (alId1 == alId2) {
                            artUri= ContentUris.withAppendedId(albumUri, alId2)
                            matchedAlbumArtPosition = alCursor.position
                            break
                        }
                    } while (alCursor.moveToNext())
                }
                val id = aCursor.getLong(idIndex)
                val uri = ContentUris.withAppendedId(audioUri, aCursor.getLong(idIndex))
                val artist = aCursor.getString(artistIndex)
                val duration = aCursor.getInt(durationIndex)
                val size = aCursor.getLong(sizeIndex)
                val name = aCursor.getString(nameIndex)
                val modified = aCursor.getLong(modifiedIndex)
                val albumArtData : String? = if(matchedAlbumArtPosition != null && matchedAlbumArtPosition >= 0) {
                    alCursor?.moveToPosition(matchedAlbumArtPosition)
                    alCursor?.getString(albumArtDataIndex ?: -1)
                } else null
                audioList.add(
                    MediaData(
                        id, uri, name ?: "Unknown", artist ?: "Unknown", duration,
                        artUri, size, modified, FileType.FileTypeAudio, albumArtData
                    )
                )
            } while (aCursor.moveToNext())
        }
        aCursor?.close()
        alCursor?.close()
        audioList.sortBy { it.title.lowercase() }
        return audioList
    }

    fun generateVideoList() : List<MediaData> {
        videosList.clear()
        val vURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.query(vURI,null,null,null,null).use { vCursor ->
            if (vCursor!=null && vCursor.moveToFirst()) {

                val idIndex = vCursor.getColumnIndex(MediaStore.Video.Media._ID)
                val titleIndex = vCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val durationIndex = vCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeIndex = vCursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val lastModifiedIndex = vCursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val mediaDataPathIndex = vCursor.getColumnIndex(MediaStore.Video.Media.DATA) // only for SDK < Q

                do {
                    val id = vCursor.getLong(idIndex)
                    val uri = ContentUris.withAppendedId(vURI, id)
                    val title = vCursor.getString(titleIndex)
                    val duration = vCursor.getInt(durationIndex)
                    val size = vCursor.getLong(sizeIndex)
                    val modified = vCursor.getLong(lastModifiedIndex)
                    val pathData = vCursor.getString(mediaDataPathIndex)
                    videosList.add(MediaData(id, uri, title, null, duration, uri, size, modified, FileType.FileTypeVideo,
                        pathData))
                } while (vCursor.moveToNext())
            }
        }
        videosList.sortByDescending { videoObject -> videoObject.lastModified }
        return videosList
    }
}