package chad.orionsoft.sendit.utils

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
}