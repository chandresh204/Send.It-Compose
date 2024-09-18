package chad.orionsoft.sendit.domain

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object QRDomain {

    fun getQRBitmap(msg: String, size : Int) : Bitmap? {
        val result = try {
            MultiFormatWriter().encode(msg, BarcodeFormat.QR_CODE, size, size)
        } catch (e: Exception) {
            Log.e("getQRBitmap", "$e")
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w*h)

        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if(result.get(x,y)) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0,size, 0,0,w,h)
        return bitmap
    }
}