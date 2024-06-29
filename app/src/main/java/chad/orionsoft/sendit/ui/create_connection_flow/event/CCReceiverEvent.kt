package chad.orionsoft.sendit.ui.create_connection_flow.event

sealed class CCReceiverEvent {

    data class OnCodeEntered(val code: String): CCReceiverEvent()
    data object OnCancelSearch : CCReceiverEvent()
}