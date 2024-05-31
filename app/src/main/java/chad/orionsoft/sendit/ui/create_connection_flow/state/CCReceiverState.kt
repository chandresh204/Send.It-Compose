package chad.orionsoft.sendit.ui.create_connection_flow.state

data class CCReceiverState(
    val currentStep : ReceiverConnectStep = ReceiverConnectStep.Waiting,
    val senderName: String? = null
)

enum class ReceiverConnectStep {
    CheckIsWLAN, Waiting, EnterCode, FinalHandshake, Connected
}