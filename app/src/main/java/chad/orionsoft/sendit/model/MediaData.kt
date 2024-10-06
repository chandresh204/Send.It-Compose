package chad.orionsoft.sendit.model

import android.net.Uri

data class MediaData(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist : String?,
    val duration : Int,
    val thumbnail: Uri,
    val size: Long,
    val lastModified: Long,
    val mediaType: FileType,
    val pathData : String? = null, // only for sdk < Q, used as AlbumArtPath only in audio
)