package chad.orionsoft.sendit.ui.qr_connect

import android.graphics.Bitmap
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.model.ConnectedDeviceDetails

data class QrConnectState(
    val qrBitmap : Bitmap? = null,
    val connectStatus: ConnectStatus = ConnectStatus.NoNetwork,
    val pairingDeviceDetails: ConnectedDeviceDetails? = null,
    val qrScanResult: String = "",
    val connectionError: Pair<String,String>? = null, // Pair of title and description
)