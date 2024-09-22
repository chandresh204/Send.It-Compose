package chad.orionsoft.sendit.utils

object Constants {
    const val OUTLINE_TEXT_FIELD_RADIUS = 16
    const val PAGE_PADDING = 16
    const val DIALOG_PADDING = 8
    const val DIALOG_CORNER = 24
    const val SCAN_QR_TIME_OUT = 60 // seconds

    const val APP_CODE = "zôrC*jìéHÊ3mFF¾4ÕúëHXåÐ"

    // triple data pattern == ((data1))data2/*data3*/

    const val DEVICE_TYPE_PHONE = "siopa"
    const val DEVICE_TYPE_DESKTOP = "lyiop"

    const val MIN_SUPPORT_VERSION = 300
    const val CURRENT_APP_SUPPORT_VERSION = 300

    const val TCP_MODE_STR = "3á&ĉ_ĝcĖÄA¹ÙÒ:ÿ¸ûoò.Ğ#ĤM" // + ((mode))count/*size*/

    const val MODE_TYPE_FILE = "file"
    const val MODE_TYPE_DISCONNECT = "disconnect"

    const val TCP_CONTINUE = ",brÃDr½çÈlĐē'÷"
    const val TCP_OKAY = "d%@^"
    const val TCP_DONE = "¹ÙÒ:ÿ"
    const val TCP_FILE_INFO = "e398jfd%@^"
    const val TCP_FILE_IGNORE = "dksjdlascnhlksadkhw"
    const val TCP_FILE_FREE_SPACE_SIGNAL  = "sdnccop5d75sde398jfd%@^"

    // QR Connect Process
    const val QR_CODE_SIZE = 512
    // QR INFORMATION
    const val QR_INFO = APP_CODE // + triple(deviceType+username, wifiAddress: Port, color)
    // QR Host
    const val QR_CONNECT_REPLY = "[Õæs·}dÙÁÂp£x¡g¯OÕ£°ç£H¦ö"  // + /*freeSpace*/
    // QR Scanner
    const val QR_CONNECT_SIG = APP_CODE // + triple(username, color, freeSpace) *step1

    // Device pair status check
    const val UDP_IS_ALIVE = APP_CODE

    // const val TCP_FILE_INFO = TCP_FILE_CONTINUE((filename))type/*size*/

//    // file types
//    const val FILE_TYPE_IMAGE = "Image"
//    const val FILE_TYPE_AUDIO = "audio"
//    const val FILE_TYPE_VIDEO = "video"
//    const val FILE_TYPE_OTHER = "other"

    // work manager params
    const val SERVER_WM_TAG = "tcpServer"
    const val SENDER_WM_TAG = "tcpSender"
    const val TCP_SERVER_WM_PORT = "receiverPort"
    const val TCP_SERVER_WM_DEVICE = "deviceName"

    const val UPDATE_TRANSFER_STATUS_AFTER_LOOPS = 300

    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".heic", ".svg")
    val audioExtensions = listOf(".mp3", ".m4a", ".aac")
    val videoExtensions = listOf(".mp4", ".avi", ".mkv", ".m4v")
}