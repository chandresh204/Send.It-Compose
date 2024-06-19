package chad.orionsoft.sendit.ui.create_connection_flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetProgressOnly
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetSearching
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetShowConnectCode
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith1Button
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.bottom_sheets.WLANConnectionRequiredBottomSheet
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCSenderEvent
import chad.orionsoft.sendit.ui.create_connection_flow.state.ConnectStep
import chad.orionsoft.sendit.ui.create_connection_flow.view_model.CCSenderViewModel

@Composable
fun CreateConnectionSender(onFinish: () -> Unit) {

    val vModel = hiltViewModel<CCSenderViewModel>()
    val uiState by vModel.ccSenderState.collectAsState()

    LaunchedEffect(true) {
        vModel.finishReceiver.collect {
            onFinish()
        }
    }

    when(uiState.currentStep) {
        ConnectStep.AskToConnect -> {
            BottomSheetWith2Buttons(
                title = "Connect to Receiver Now?",
                positiveButton = { TextWithIcon(Icons.Default.Check, text = "Now") },
                negativeButton = { TextWithIcon(Icons.Default.Close, text = "Later") },
                positiveButtonClick = {
                    vModel.onEvent(CCSenderEvent.OnStartConnectionProcess)
                },
                negativeButtonClick = onFinish
            ) {

            }
        }

        ConnectStep.CheckIsWLAN -> {
            val context = LocalContext.current
            WLANConnectionRequiredBottomSheet(onFinish, {
                StartActivity.openWifiSettings(context)
            }) {
                StartActivity.openTetherSettings(context)
            }
        }

        ConnectStep.SearchReceiver -> {
            BottomSheetSearching(title = "Searching For Receiver", searched = uiState.receiverList,
                onReceiverSelected = {
                    vModel.onEvent(CCSenderEvent.OnReceiverSelected(it))
                }) {
                vModel.onEvent(CCSenderEvent.OnCancelProcess)
            }
        }

        ConnectStep.ShowConnectCode -> {
            BottomSheetShowConnectCode(title = "Connect Code",
                subtitle = "Ask Receiver to Enter below code to connect", code = "1234", bottomLine = "") {
                vModel.onEvent(CCSenderEvent.OnCancelProcess)
            }
        }
        ConnectStep.FinalHandshake -> {
            BottomSheetProgressOnly(title = "Connecting to ${uiState.selectedReceiver}")
        }

        ConnectStep.Connected -> {
            BottomSheetWith1Button(
                title = "Connected to ${uiState.selectedReceiver}",
                subtitle = "",
                negativeButton = {
                                 TextWithIcon(Icons.Default.Check, text = "Done")
                }) {
                vModel.onEvent(CCSenderEvent.OnCancelProcess)
            }
        }
    }

}