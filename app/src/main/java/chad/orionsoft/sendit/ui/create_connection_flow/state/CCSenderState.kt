package chad.orionsoft.sendit.ui.create_connection_flow.state

import java.net.InetAddress

data class CCSenderState(
    val currentStep : ConnectStep = ConnectStep.AskToConnect,
    val receiverList : List<ReceiverData> = emptyList(),
    val receiverListToDisplay : List<String> = emptyList(),
    val selectedReceiver: ReceiverData? = null
)

data class ReceiverData(
    val address: InetAddress,
    val name: String
)

enum class ConnectStep {
    AskToConnect, CheckIsWLAN, SearchReceiver, ShowConnectCode, FinalHandshake, Connected
}