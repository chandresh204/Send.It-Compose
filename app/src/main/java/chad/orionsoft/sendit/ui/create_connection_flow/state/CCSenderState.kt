package chad.orionsoft.sendit.ui.create_connection_flow.state

data class CCSenderState(
    val isOnWLAN : Boolean = true,
    val connectToReceiverNowDialog: Boolean = true,
)