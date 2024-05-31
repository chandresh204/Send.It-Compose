package chad.orionsoft.sendit.ui.create_connection_flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetProgressOnly
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetSearching
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith1Button
import chad.orionsoft.sendit.ui.bottom_sheets.WLANConnectionRequiredBottomSheet
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCReceiverEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.view_model.CCReceiverViewModel
import chad.orionsoft.sendit.ui.dialogs.EnterValueDialog

@Composable
fun CreateConnectionReceiver(onFinish : (Boolean) -> Unit) {

    val vModel = hiltViewModel<CCReceiverViewModel>()
    val uiState by vModel.receiverState.collectAsState()

    LaunchedEffect(true) {
        vModel.finishReceiver.collect {
            onFinish(it)
        }
    }

    when(uiState.currentStep) {
        ReceiverConnectStep.Waiting -> {
            BottomSheetSearching(title = "Waiting for sender",
                searched = emptyList(), onReceiverSelected = {}) {
                onFinish(false)
            }
        }

        ReceiverConnectStep.CheckIsWLAN -> {
            WLANConnectionRequiredBottomSheet(
                onDismiss = {  },
                connectToWifiClick = { }) {

            }
        }

        ReceiverConnectStep.EnterCode -> {
            EnterValueDialog(
                title = "Enter Code",
                subtitle = "Enter Code Receive file form ${uiState.senderName}", onCodeSubmit = {
                    vModel.onEvent(CCReceiverEvent.OnCodeEntered(it))
                }) {

            }
        }
        ReceiverConnectStep.FinalHandshake -> {
            BottomSheetProgressOnly(title = "Connecting to ${uiState.senderName}")
        }
        ReceiverConnectStep.Connected -> {
            BottomSheetWith1Button(
                title = "Connected to ${uiState.senderName}",
                subtitle = "",
                negativeButton = {
                    TextWithIcon(Icons.Default.Check, text = "Done")
                }) {}
        }
    }
}