package chad.orionsoft.sendit.model

import androidx.compose.ui.graphics.Color
import java.net.InetAddress

data class ConnectedDeviceDetails(
    val name: String,
    val address: InetAddress,
    val deviceType: String,
    val color: Color,
    val freeSpace: Long,
    val serverPort: Int
)