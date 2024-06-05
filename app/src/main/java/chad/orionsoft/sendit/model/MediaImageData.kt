package chad.orionsoft.sendit.model

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