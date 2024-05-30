package chad.orionsoft.sendit.ui.create_connection_flow.state

data class CCSenderState(
    val currentStep : ConnectStep = ConnectStep.AskToConnect,
    val selectedReceiver: String? = null
)

enum class ConnectStep {
    AskToConnect, CheckIsWLAN, SearchReceiver, ShowConnectCode, FinalHandshake, Connected
}