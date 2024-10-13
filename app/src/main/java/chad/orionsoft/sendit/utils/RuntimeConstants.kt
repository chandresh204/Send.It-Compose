package chad.orionsoft.sendit.utils

import android.os.Build
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.repository.PreferenceRepository
import kotlin.random.Random

object RuntimeConstants {

    var screenWidthDp : Dp = 360.dp // need to be calculated once in Splash screen
    var screenHeightDp : Dp = 720.dp

    var BROAD_ADDR : String? = null

//    var connectedDeviceDetails : ConnectedDeviceDetails? = null

    var myColor : String = "0033EE"

    fun generateRandomCode(digit: Int) : String {
        val sb = StringBuilder()
        for (i in 0 until digit) {
            if(i == 0) {
                sb.append(Random.nextInt(1, 10))
            } else {
                sb.append(Random.nextInt(10))
            }
        }
        return sb.toString()
    }

    /**
     * gets name from [PreferenceRepository] , then adds device Manufacturer and model
     */
    fun createMyUserName(preferenceRepository: PreferenceRepository) : String {
        val userName = preferenceRepository.getUserNameFromPreferences()
        return "$userName - ${Build.MANUFACTURER}:${ Build.MODEL}"
    }

    /**
     * adds device Manufacturer and model into supplied string
     */
    fun createMyUserName(name: String) : String = "$name - ${Build.MANUFACTURER}:${ Build.MODEL}"

    fun createAppColorList() : List<ColorConstants> {
        val appColors = mutableListOf<ColorConstants>()
        ColorConstants.entries.forEach {
            if(it == ColorConstants.Device && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return@forEach
            }
            appColors.add(it)
        }
        return appColors
    }
}