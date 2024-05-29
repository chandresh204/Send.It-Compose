package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSearching(title:String, searched: List<String>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val hideAndCloseSheet = {
        coroutineScope.launch {
            sheetState.hide()
            delay(500)
            onDismiss()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(Constants.PAGE_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { hideAndCloseSheet() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )) {
                    TextWithIcon(Icons.Default.Close,
                        text = stringResource(R.string.close))
                }
            }
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn {
                items(searched) {
                    SearchedItem(it) {
                        hideAndCloseSheet()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedItem(title: String, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth().clickable(onClick = onClick)
        .padding(vertical = 8.dp)) {
        Text(text = title, modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        BottomSheetSearching("Searching for receiver", listOf("device 1", "Device 2", "device 3")) {}
    }
}