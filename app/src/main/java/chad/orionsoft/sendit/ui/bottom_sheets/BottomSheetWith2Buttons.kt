package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.theme.SendItTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetWith2Buttons(
    title: String,
    positiveButton: @Composable () -> Unit,
    negativeButton: @Composable () -> Unit,
    positiveButtonClick: () -> Unit,
    negativeButtonClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val hideAndCloseSheet : (action: () -> Unit) -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            delay(500)
            it()
            onDismiss()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = sheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    hideAndCloseSheet(
                        positiveButtonClick
                    )
                }, modifier = Modifier.weight(1f), content = { positiveButton() })
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedButton(onClick = {
                    hideAndCloseSheet(
                        negativeButtonClick
                    )
                },modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )) { negativeButton() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        BottomSheetWith2Buttons("Connect To Receiver Now?",
            positiveButton = { TextWithIcon(Icons.Default.Check, text = "Yes") },
            negativeButton = { TextWithIcon(Icons.Default.Close, text = "No")  },
            {}, {}) {}
    }
}