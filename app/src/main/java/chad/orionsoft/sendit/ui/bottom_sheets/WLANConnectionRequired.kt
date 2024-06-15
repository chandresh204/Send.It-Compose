package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WLANConnectionRequiredBottomSheet(
    onDismiss: () -> Unit,
    connectToWifiClick: () -> Unit,
    createNetworkClick: () -> Unit
) {
    // TODO add cancel button and
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )
    val coroutineScope = rememberCoroutineScope()
    val closeNow : () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            delay(500)
            onDismiss()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Constants.DIALOG_PADDING.dp)
        ) {
            Text(text = stringResource(id = R.string.wlan_connection_required),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
            Text(text = stringResource(id = R.string.wlan_explanation))
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
            Text(text = stringResource(id = R.string.wlan_connect_to_wifi))
            OutlinedButton(onClick = connectToWifiClick) {
                Text(text = stringResource(id = R.string.button_connect_to_wifi))
            }
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
            Text(text = "--" + stringResource(id = R.string.or) + "--")
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
            Text(text = stringResource(id = R.string.wlan_create_network))
            OutlinedButton(onClick = createNetworkClick) {
                Text(text = stringResource(id = R.string.button_create_network))
            }
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
            TextButton(onClick = closeNow) {
                Text(text = "Abort for now", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.padding(vertical = 9.dp))
        }
    }
}