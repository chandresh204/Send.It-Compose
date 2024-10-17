package chad.orionsoft.sendit

import org.junit.Test
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

    }

    @Test
    fun empty_uri_thumbnail_file_name() {
        val emptyUri = ""
        val fileName = emptyUri.run {
            substring(this.lastIndexOf("/")+1)
        }
        print("result >>> $fileName\n")
    }

    private fun generateRandomString() : String {
        var randomStr = ""
        for (i in 0 until 32) {
            randomStr += Random.nextInt(300).toChar()
        }
        return randomStr
    }
}