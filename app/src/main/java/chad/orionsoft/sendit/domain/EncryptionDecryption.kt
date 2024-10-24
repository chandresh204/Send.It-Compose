package chad.orionsoft.sendit.domain

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object EncryptionDecryption {


    private const val algorithm = "AES/CBC/PKCS5Padding"
    private val key = SecretKeySpec("=34LsxOe(d%mNsIw".toByteArray(), "AES")
    private val iv = IvParameterSpec(ByteArray(16))

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(cipherText: String): String {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val plainText = cipher.doFinal(Base64.decode(cipherText))
        return String(plainText)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(inputText: String): String {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val cipherText = cipher.doFinal(inputText.toByteArray())
        return Base64.encode(cipherText)
    }
}