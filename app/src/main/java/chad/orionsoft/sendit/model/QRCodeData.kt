package chad.orionsoft.sendit.model

data class QRCodeData(
    val deviceType: String,
    val address: String,
    val port: Int,
    val color: String,
    val userName: String,
    val appVersion: Int
)