package chad.orionsoft.sendit.ui.create_connection_flow.state

import java.net.InetAddress

data class CCSenderState(
    val currentStep : ConnectStep = ConnectStep.AskToConnect,
    val receiverList : List<ReceiverData> = emptyList(),
    val receiverListToDisplay : List<String> = emptyList(),
    val connectCode: String? = null,
    val enterCodeTimer: Int = -1,
    val error: String? = null
)

data class ReceiverData(
    val address: InetAddress,
    val name: String,
    val port : Int = 4444 // default only for receiver
)

enum class ConnectStep {
    AskToConnect, CheckIsWLAN, SearchReceiver, ShowConnectCode, FinalHandshake, Connected, Error
}