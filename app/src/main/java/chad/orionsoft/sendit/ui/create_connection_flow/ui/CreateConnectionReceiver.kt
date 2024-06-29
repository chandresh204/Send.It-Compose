package chad.orionsoft.sendit.ui.create_connection_flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
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
    val senderDetails = vModel.senderDetails

    LaunchedEffect(true) {
        vModel.finishReceiver.collect {
            onFinish(it)
        }
    }

    when(uiState.currentStep) {
        ReceiverConnectStep.Waiting -> {
            BottomSheetSearching(title = "Waiting for sender",
                searched = emptyList(), onOptionSelected = {}) {
                vModel.onEvent(CCReceiverEvent.OnCancelSearch)
            }
        }

        ReceiverConnectStep.CheckIsWLAN -> {
            val context = LocalContext.current
            WLANConnectionRequiredBottomSheet({ onFinish(false) }, {
                StartActivity.openWifiSettings(context)
            }) {
                StartActivity.openTetherSettings(context)
            }
        }

        ReceiverConnectStep.EnterCode -> {
            EnterValueDialog(
                title = "Enter Code",
                subtitle = "Enter Code Receive file form ${senderDetails?.name}",
                timerValue = uiState.enterCodeTimer ?: -1,
                onCodeSubmit = {
                    vModel.onEvent(CCReceiverEvent.OnCodeEntered(it))
                }) {

            }
        }
        ReceiverConnectStep.FinalHandshake -> {
            BottomSheetProgressOnly(title = "Connecting to ${senderDetails?.name}")
        }
        ReceiverConnectStep.Connected -> {
            BottomSheetWith1Button(
                title = "Connected to ${senderDetails?.name}",
                subtitle = "",
                negativeButton = {
                    TextWithIcon(Icons.Default.Check, text = "Done")
                }) {
                onFinish(true)
            }
        }

        ReceiverConnectStep.Error -> {
            BottomSheetWith1Button(
                title = uiState.error ?: stringResource(R.string.an_error_occurred),
                subtitle = "",
                negativeButton = {
                    TextWithIcon(Icons.Default.Close, text = "Close")
                }, { onFinish(false) })
        }

        ReceiverConnectStep.EnterCodeResult -> {
            BottomSheetWith1Button(
                title = "Entered Code result",
                subtitle = "",
                negativeButton = {
                    TextWithIcon(Icons.Default.Close, text = "Close")
                }, { onFinish(false) })
        }
    }
}