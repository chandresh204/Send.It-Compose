package chad.orionsoft.sendit.model

import android.net.Uri
import java.io.File

data class MediaImageData(
    val id:Long ,
    val imageDATA: String,
    val title:String,
    var thumbData:String
) {
    private val imageFile = File(imageDATA)
    val size=imageFile.length()
    val lastModified=imageFile.lastModified()
}

data class MediaImageDataQ(val uri: Uri,
                           val title:String,
                           val size:Long,
                           val lastModified:Long)