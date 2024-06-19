package chad.orionsoft.sendit.ui.create_connection_flow.event

sealed class CCSenderEvent {
    data object OnStartConnectionProcess: CCSenderEvent()
    data object OnCancelProcess : CCSenderEvent()

    data class OnReceiverSelected(val name: String): CCSenderEvent()
}