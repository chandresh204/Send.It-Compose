package chad.orionsoft.sendit.ui.qr_connect

import android.graphics.Bitmap

data class QrConnectState(
    val canShowQR : Boolean = false,
    val qrBitmap : Bitmap? = null,
    val isLoading : Boolean = true
)