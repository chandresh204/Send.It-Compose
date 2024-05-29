package chad.orionsoft.sendit.ui.create_connection_flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.bottom_sheets.WLANConnectionRequiredBottomSheet
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.create_connection_flow.event.CCSenderEvent
import chad.orionsoft.sendit.ui.create_connection_flow.view_model.CCSenderViewModel

@Composable
fun CreateConnectionSender(onFinish: () -> Unit) {

    val vModel = hiltViewModel<CCSenderViewModel>()
    val uiState by vModel.ccSenderState.collectAsState()

    if(uiState.connectToReceiverNowDialog) {
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
0
    if(!uiState.isOnWLAN) {
        WLANConnectionRequiredBottomSheet({

        }, { }) {

        }
    }
}