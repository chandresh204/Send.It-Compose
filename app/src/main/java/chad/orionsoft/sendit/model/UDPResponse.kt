package chad.orionsoft.sendit.model

import java.net.InetAddress

data class UDPResponse(
    val success: Boolean,
    val partnerAddress: InetAddress,
    val partnerPort: Int,
    val response: String,
)