package chad.orionsoft.sendit.ui.create_connection_flow.event

import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData

sealed class CCSenderEvent {
    data object OnStartConnectionProcess: CCSenderEvent()
    data object OnCancelProcess : CCSenderEvent()

    data class OnReceiverSelected(val receiverData: ReceiverData): CCSenderEvent()
}