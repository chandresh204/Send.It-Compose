package chad.orionsoft.sendit.model

//TODO put in pro guard rules
data class QRCodeData(
    val deviceType: String,
    val address: String,
    val port: Int,
    val color: String,
    val userName: String,
    val appVersion: Int // TODO implement its logic in next release
)