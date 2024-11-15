package chad.orionsoft.sendit.model

data class DeviceHandshakeData(
    val name : String,
    val deviceType: String,
    val color : String,
    val freeSpace : Long,
    val appVersion : Int
)