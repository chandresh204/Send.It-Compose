package chad.orionsoft.sendit.utils

import android.net.Uri
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

    fun formatRateWithUnit(transferred: Long) : Pair<String, String> {
        val rateStr : String
        val rateUnit : String
        when {
            transferred < 1024 -> {
                rateStr = "$transferred"
                rateUnit = "B/s"
            }
            transferred in 1024 .. 1048575 -> {
                rateStr = "${transferred/1024}"
                rateUnit = "KB/s"
            }
            else -> {
                val inMB1 = (transferred.toFloat() / 1048576)
                val inMB2 = inMB1 * 100
                val inMB3 = inMB2.roundToLong()
                val inMB = (inMB3.toFloat() / 100)
                rateStr = "$inMB"
                rateUnit = "MB/s"
            }
        }
        return Pair(rateStr, rateUnit)
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

    fun convertColorToString(color: Color) : String {
        val redInt = color.red * 255
        val greenInt = color.green *255
        val blueInt = color.blue * 255
        return redInt.toInt().toString(16).padStart(2, '0') +
                greenInt.toInt().toString(16).padStart(2, '0') +
                blueInt.toInt().toString(16).padStart(2, '0')
    }

    fun convertStringToColor(colorStr: String) : Color {
        val red = colorStr.substring(0, 2)
        val green = colorStr.substring(2,4)
        val blue = colorStr.substring(4,6)
        return Color(
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
               R.drawable.image_24px
           }
            FileType.FileTypeAudio -> {
                R.drawable.music_note_24px
            }
            FileType.FileTypeVideo -> {
                R.drawable.play_circle_24px
            }
            FileType.FileTypeOther -> {
                R.drawable.file_present_24px
            }
        }
    }

    fun getPlaceHolderIconsAsPerFileName(filename: String) : Int {
        val lastIndexOfDot = filename.lastIndexOf(".")
        if (lastIndexOfDot < 0) return R.drawable.file_present_24px
        val extension = filename.substring(lastIndexOfDot).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> R.drawable.music_note_24px
            Constants.imageExtensions.contains(extension) -> R.drawable.image_24px
            Constants.videoExtensions.contains(extension) -> R.drawable.play_circle_24px
            extension.contains("pdf") -> R.drawable.picture_as_pdf_24px
            extension.contains("apk") -> R.drawable.android_24px
            extension.contains("exe") -> R.drawable.windows_exe
            extension.contains("msi ") -> R.drawable.windows_exe
            extension.contains("zip") -> R.drawable.folder_zip_24px
            extension.contains("7z") -> R.drawable.folder_zip_24px
            extension.contains("rar") -> R.drawable.folder_zip_24px
            extension.contains("tar") -> R.drawable.folder_zip_24px
            else -> R.drawable.file_present_24px
        }
    }

    fun getFileTypeFromFileName(filename: String) : FileType {
        val lastIndexOfDot = filename.lastIndexOf(".")
        if(lastIndexOfDot < 0) return FileType.FileTypeOther
        val extension = filename.substring(filename.lastIndexOf(".")).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> FileType.FileTypeAudio
            Constants.imageExtensions.contains(extension) -> FileType.FileTypeImage
            Constants.videoExtensions.contains(extension) -> FileType.FileTypeVideo
            else -> FileType.FileTypeOther
        }
    }

    fun getMimeTypeFromFileName(filename: String) : String {
        val lastIndexOfDot = filename.lastIndexOf(".")
        if(lastIndexOfDot < 0) return "*/*"
        val extension = filename.substring(filename.lastIndexOf(".")).lowercase()
        return when {
            Constants.audioExtensions.contains(extension) -> "audio/*"
            Constants.imageExtensions.contains(extension) -> "image/*"
            Constants.videoExtensions.contains(extension) -> "video/*"
            else -> "*/*"
        }
    }

    fun separateAddressAndPort(addressWithPort: String) : Pair<String, Int> {
        val address = addressWithPort.split(":")[0]
        val port = addressWithPort.split(":")[1].toInt()
        return Pair(address, port)
    }

    fun extractLastElementOfUri(uri: Uri) : String =
        uri.toString().run {
            substring(this.lastIndexOf("/")+1)
        }

    fun encodeUriPath(path: String) : String = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())

    fun decodeUriPath(encoded: String) : String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())

    fun formatDateTime(time: Long) : String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        val formatter = Formatter()
        return formatter.format("%tD %tT", cal, cal).toString()
    }
}