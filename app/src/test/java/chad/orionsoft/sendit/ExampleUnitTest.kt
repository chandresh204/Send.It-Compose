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
        val charSequence = mutableListOf<Char>()
        for(i in 0 until 32) {
            val charInt = Random.nextInt(255)
            charSequence.add(Char(charInt))
        }
        println(String(charSequence.toCharArray()))
    }
}