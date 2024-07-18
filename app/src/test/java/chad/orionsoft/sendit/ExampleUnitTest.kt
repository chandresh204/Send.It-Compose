package chad.orionsoft.sendit

import androidx.compose.ui.graphics.Color
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
       convertStringToColor("FF2566")
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
            blue = green.toInt(radix = 16)/255f
        )
    }
}