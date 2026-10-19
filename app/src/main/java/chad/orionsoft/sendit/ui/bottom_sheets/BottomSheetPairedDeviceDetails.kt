package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.ui.components.CharInCircle
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPairedDeviceDetails(cDevice: ConnectedDeviceDetails,
                                   sent : String,
                                   received: String,
                                   onViewClicked: () -> Unit,
                                   onDisconnectClicked : () -> Unit,
                                   onDismiss : () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    val coroutineScope = rememberCoroutineScope()
    val hideAndCloseSheet = {
        coroutineScope.launch {
            sheetState.hide()
            delay(500)
            onDismiss()
        }
    }
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
        hideAndCloseSheet()
    }) {
        SheetContent(
            modifier = Modifier.padding(16.dp),
            cDevice = cDevice,
            sent = sent,
            received = received,
            onViewClicked = onViewClicked,
            onDisconnectClicked = onDisconnectClicked) {
            hideAndCloseSheet()
        }
    }
}

@Composable
private fun SheetContent(modifier: Modifier= Modifier,
                         cDevice: ConnectedDeviceDetails,
                         sent : String,
                         received: String,
                         onViewClicked: () -> Unit,
                         onDisconnectClicked : () -> Unit,
                         onDismiss : () -> Unit) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            CharInCircle(char = cDevice.name.first(),
                color = cDevice.color, size = 50)
            Column {
                Text(text = stringResource(R.string.connected_to))
                Text(text = cDevice.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "${stringResource(R.string.available_space)} " +
                        ": ${Converters.formatDataString(cDevice.freeSpace)}")
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "${stringResource(R.string.sent)}: $sent")
            Text(text = "${stringResource(R.string.received)} : $received")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onViewClicked) {
                Text(text = stringResource(R.string.view_list))
            }
            Button(onClick = onDisconnectClicked, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )) {
                Text(text = stringResource(R.string.disconnect))
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss))
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SheetContent(
        modifier = Modifier.padding(16.dp),
        cDevice = ConnectedDeviceDetails(
            "Chad - Moto",
            InetAddress.getLocalHost(),
            Constants.DEVICE_TYPE_PHONE,
            Color.Cyan,
            589564L,
            22
        ),
        sent = "144 MB",
        received = "295 GB",
        onDismiss = {},
        onViewClicked = {},
        onDisconnectClicked = {}
    )
}