package chad.orionsoft.sendit.ui.transfer_process_screen

data class TransferMainState(
    val sendingItemCount: String = "",
    val sendingSpeed: String = "",
    val receivingItemCount: String = "",
    val receivingItemSpeed: String = "",
    val totalSentAndReceived: Pair<String, String> = Pair("0B", "0B")
)