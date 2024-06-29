package chad.orionsoft.sendit.model

import java.net.InetAddress

data class ConnectedDeviceDetails(
    val name: String,
    val address: InetAddress,
    val freeSpace: Long
)