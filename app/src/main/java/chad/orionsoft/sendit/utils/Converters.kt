package chad.orionsoft.sendit.utils

import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.model.FileType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Formatter
import kotlin.math.roundToLong

object Converters {

    fun formatDataString(bytes: Long,separatorChar:Char = ' '): String {
        val str:String
        when {
            bytes < 1024 -> str = "$bytes${separatorChar}B"
            bytes in 1024..1048575 -> {
                val inKB = (bytes / 1024)
                str = "$inKB${separatorChar}kB"
            }
            bytes in 1048576..1073741823 -> {
                val inMB1 = (bytes.toFloat() / 1048576)
                val inMB2 = inMB1 * 100
                val inMB3 = inMB2.roundToLong()
                val inMB = (inMB3.toFloat() / 100)
                str = "$inMB${separatorChar}MB"
            }
            else -> {
                val inGB1 = (bytes.toFloat() / 1073741824)
                val inGB2 = inGB1 * 100
                val inGB3 = inGB2.roundToLong()
                val inGB = (inGB3.toFloat() / 100)
                str = "$inGB${separatorChar}GB"
            }
        }
        return str
    }

    fun formatDuration(duration: Int): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = duration.toLong() - cal.timeZone.rawOffset
        val formatter = Formatter()
        val hr = cal.get(Calendar.HOUR)
        return if (hr > 0) {
            formatter.format("%tH:%tM:%tS", cal, cal, cal).toString()
        } else {
            formatter.format("%tM:%tS", cal, cal).toString()
        }
    }

    fun extractDataInNormalCase(response: String) : String {
        val startIndex = response.indexOf("/*") + 2
        val endIndex = response.indexOf("*/")
        return response.substring(startIndex, endIndex)
    }

    fun createTripleData(header: String, first: Any, second: Any, third: Any) : String =
        "$header(($first))$second/*$third*/"

    fun extractTripleData(response: String) : Triple<String, String, String> {
        val firstIndex = response.indexOf("((")
        val secondIndex = response.indexOf("))")
        val thirdIndex = response.indexOf("/*")
        val finalIndex = response.indexOf("*/")
        val data1 = response.substring(firstIndex+2, secondIndex)
        val data2 = response.substring(secondIndex+2, thirdIndex)
        val data3 = response.substring(thirdIndex+2, finalIndex)
        return Triple(data1, data2, data3)
    }

    fun convertStringToColor(colorStr: String) : Color {
        val red = colorStr.substring(0, 2)
        val green = colorStr.substring(2,4)
        val blue = colorStr.substring(4,6)
        println(red.toInt(radix = 16)/255f)
        return Color.Unspecified.copy(
            alpha = 1f,
            red = red.toInt(radix = 16)/255f,
            green = green.toInt(radix = 16)/255f,
            blue = blue.toInt(radix = 16)/255f
        )
    }

    fun convertToFileType(typeString: String) : FileType {
        var type = FileType.FileTypeOther
        FileType.entries.forEach {
            if(typeString == it.title) {
                type = it
            }
        }
        return type
    }

    fun getPlaceHolderIconsAsPerMediaType(mediaType : FileType) : Int {
        return when(mediaType) {
           FileType.FileTypeImage -> {
               R.drawable.image_icon_small
           }
            FileType.FileTypeAudio -> {
                R.drawable.music_icon_small
            }
            FileType.FileTypeVideo -> {
                R.drawable.video_icon_small
            }
            FileType.FileTypeOther -> {
                R.drawable.file_unknown_icon_small
            }
        }
    }

    fun getPlaceHolderIconsAsPerFileName(filename: String) : Int {
        val extension = filename.substring(filename.lastIndexOf(".")).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> R.drawable.music_icon_small
            Constants.imageExtensions.contains(extension) -> R.drawable.image_icon_small
            Constants.videoExtensions.contains(extension) -> R.drawable.video_icon_small
            else -> R.drawable.file_unknown_icon_small
        }
    }

    fun getFileTypeFromFileName(filename: String) : FileType {
        val extension = filename.substring(filename.lastIndexOf(".")).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> FileType.FileTypeAudio
            Constants.imageExtensions.contains(extension) -> FileType.FileTypeImage
            Constants.videoExtensions.contains(extension) -> FileType.FileTypeVideo
            else -> FileType.FileTypeOther
        }
    }

    fun getMimeTypeFromFileName(filename: String) : String {
        val extension = filename.substring(filename.lastIndexOf(".")).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> "audio/*"
            Constants.imageExtensions.contains(extension) -> "image/*"
            Constants.videoExtensions.contains(extension) -> "video/*"
            else -> "*/*"
        }
    }

    fun encodeUriPath(path: String) : String = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())

    fun decodeUriPath(encoded: String) : String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
}