package chad.orionsoft.sendit.model

enum class ConnectStatus(var msg: String = "") {
    NoNetwork, OnHotspot, OnWifi, ConnectingDevice, DeviceConnected
}