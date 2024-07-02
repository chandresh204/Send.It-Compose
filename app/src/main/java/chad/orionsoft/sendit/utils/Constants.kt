package chad.orionsoft.sendit.utils

object Constants {
    const val OUTLINE_TEXT_FIELD_RADIUS = 16
    const val PAGE_PADDING = 16
    const val DIALOG_PADDING = 8
    const val DIALOG_CORNER = 24
    const val DEFAULT_BROADCAST_PORT= 4444
    const val ENTER_CODE_TIME_OUT = 30 // seconds

//    const val APP_CODE = "zôrC*jìéHÊ3mFF¾4ÕúëHXåÐ"

    // sender codes
    const val BROADCAST_MSG ="zôrC*jìéHÊ3mFF¾4ÕúëHXåÐ"
    const val RECEIVER_SELECT_MESSAGE = "qÒÉ4÷uìñÉE?!¯@iÝ" // + /*sender_name*/
    const val RECEIVER_NOT_SUPPORTED = "PÜ¯4Wê6C²ìútÉáé¡<«n~3tíB" // + /*reason*/
    const val CONNECT_NOW = "[Õæs·}dÙÁÂp£x¡g¯OÕ£°ç£H¦ö"
    const val ENTER_CODE_SIGNAL = "îZø¾tÇñQìÆyãç6¢NÞëÒËØé4"
    const val CODE_ENTERED_SIGNAL_RESULT = "v¬1äv@[â (õÃÅÂ>dÀÅië·À5â" // + /*true*/ or /*false*/

    // receiver code
    // BROADCAST_REPLY = "$BROADCAST_MSG/*$receiverName*/" -- for info only, do not delete
    // DEVICE_INFO_REPLY = "$RECEIVER_SELECT_MESSAGE/*uniqueId*/newport#appVersion!%freeSpace" -- for info only, do not delete
    // FREE_SPACE_SIGNAL = "$CONNECT_NOW/*freeSpace*/"  -- for info only, do not delete
    // ENTER_CODE_REPLY = "$ENTER_CODE_SIGNAL/*code*/" -- for info only, do not delete


    const val MIN_SUPPORT_VERSION = 300
    const val CURRENT_APP_SUPPORT_VERSION = 300
}