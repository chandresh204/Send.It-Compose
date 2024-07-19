package chad.orionsoft.sendit.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
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
            sb.append(Random.nextInt(10))
        }
        return sb.toString()
    }
}