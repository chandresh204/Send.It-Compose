package chad.orionsoft.sendit.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.net.InetAddress
import kotlin.random.Random

object RuntimeConstants {

    var screenWidthDp : Dp = 360.dp // need to be calculated once in Splash screen
    var screenHeightDp : Dp = 720.dp

    var BROAD_ADDR : String? = null

    var receiverAddress : InetAddress? = null

    fun generateRandomCode() : String {
        val code1= Random.nextInt(10)
        val code2=Random.nextInt(10)
        val code3=Random.nextInt(10)
        val code4=Random.nextInt(10)
        return "$code1$code2$code3$code4"
    }
}