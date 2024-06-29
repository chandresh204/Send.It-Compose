package chad.orionsoft.sendit.ui.create_connection_flow.state

data class CCReceiverState(
    val currentStep : ReceiverConnectStep = ReceiverConnectStep.Waiting,
    val error: String? = null,
    val enterCodeTimer : Int? = 0
)

enum class ReceiverConnectStep {
    CheckIsWLAN, Waiting, EnterCode, EnterCodeResult ,FinalHandshake, Connected, Error
}