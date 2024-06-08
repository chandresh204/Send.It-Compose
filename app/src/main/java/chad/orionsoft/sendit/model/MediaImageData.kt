package chad.orionsoft.sendit.model

import android.net.Uri
import java.io.File
data class MediaImageData(
    val id: Long,
    val uri: Uri,
    val title:String,
    val size:Long,
    val lastModified:Long
)