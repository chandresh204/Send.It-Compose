package chad.orionsoft.sendit.model

import android.net.Uri

data class MediaAudioData(val id: Long, val uri: Uri,
                        val name:String, val title:String, val artist:String,
                        val duration:Int, val albumArt:Uri, val size:Long, val modified : Long,
                        var isSelected:Boolean)