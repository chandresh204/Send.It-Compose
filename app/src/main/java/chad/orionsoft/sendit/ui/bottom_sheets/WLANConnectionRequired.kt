package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WLANConnectionRequiredBottomSheet(
    onDismiss: () -> Unit,
    connectToWifiClick: () -> Unit,
    createNetworkClick: () -> Unit
) {
    // TODO add cancel button and
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
        }
    }
}