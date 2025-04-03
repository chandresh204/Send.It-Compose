package chad.orionsoft.sendit

import chad.orionsoft.sendit.domain.EncryptionDecryption
import chad.orionsoft.sendit.model.QRCodeData
import chad.orionsoft.sendit.utils.Constants
import com.google.gson.Gson
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
        println(generateRandomString())
    }

    @Test
    fun encryptionDecryptionTest() {
        val qrCodeData = QRCodeData(Constants.DEVICE_TYPE_PHONE, "192.168.43.1", 4444, "ff00ff", "Chad-1234", 404)
        val gson = Gson()
        val qrJson = gson.toJson(qrCodeData, QRCodeData::class.java)
        println("qrJson >>> $qrJson")
        val encrypted = EncryptionDecryption.encrypt(qrJson)
        val decryptedText = EncryptionDecryption.decrypt(encrypted)
        println("encrypted >>> $encrypted")
        println("decrypted >>> $decryptedText")
        assert(qrJson == decryptedText)
    }

    @Test
    fun encryptionTestHelloWorld() {
        val qrString = "Hello, World!"
        val output = EncryptionDecryption.encrypt(qrString)
        assert(output == "7faniPkeIySAZfjSDsTgkQ==")
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