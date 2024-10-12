package chad.orionsoft.sendit.domain

import android.os.Build
import android.util.Log
import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.delay

class ThumbPaginator (
    val thumbnailDomain: ThumbnailDomain,
    val list: List<MediaUiData>,
    val pageSize : Int = 60
) {

    private var currentBundle: Int = 0
    private val tag = "ThumbPaginator"
    private var mutableList = list.toMutableList()


    suspend fun loadNextBundle(lastVisible: Int, updatedListCallback: (List<MediaUiData>) -> Unit) {
        val thisPageStarts = (currentBundle*pageSize)
        val thisPageEnds = (currentBundle+1)*pageSize
        if(thisPageStarts >= lastVisible) return
        currentBundle++
        for (i in thisPageStarts until thisPageEnds) {
            val media = try { mutableList[i] } catch (e:Exception) {
                break
            }
            Log.d(tag, "$i : ${mutableList[i].mediaData.title}")
            val bMap = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                thumbnailDomain.generateThumbnail(
                    Converters.extractLastElementOfUri(media.mediaData.uri),
                    media.mediaData.pathData ?: "", media.mediaData.mediaType
                )
            } else {
                thumbnailDomain.generateThumbnail(media.mediaData.uri)
            }
            val iToUpdate = mutableList.find { media.mediaData.id == it.mediaData.id }
            iToUpdate?.let {
                val index = mutableList.indexOf(it)
                val newmediaData = it.copy(
                    thumbnailBitmap = bMap
                )
                mutableList[index] = newmediaData
                updatedListCallback(mutableList)
            }
            delay(500)
        }
    }
}