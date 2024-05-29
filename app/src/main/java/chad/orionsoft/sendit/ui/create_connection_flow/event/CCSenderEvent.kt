package chad.orionsoft.sendit.ui.create_connection_flow.event

sealed class CCSenderEvent {
    data object OnStartConnectionProcess: CCSenderEvent()
}