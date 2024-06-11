package chad.orionsoft.sendit.model

import android.net.Uri

data class MediaVideoData(
    val id: Long,
    val uri: Uri,
    val title:String,
    val duration: Int,
    val size:Long,
    val lastModified : Long,
    var isSelected: Boolean
)