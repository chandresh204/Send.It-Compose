package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun BottomSheetWith1Button(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    negativeButton: @Composable () -> Unit,
    negativeButtonClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = {false}
    )
    val coroutineScope = rememberCoroutineScope()
    val hideAndCloseSheet = {
        coroutineScope.launch {
            sheetState.hide()
            delay(500)
            negativeButtonClick()
        }
    }
    ModalBottomSheet(onDismissRequest = negativeButtonClick, sheetState = sheetState) {
        Column(modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {
                    hideAndCloseSheet()
                },modifier = Modifier.weight(1f)) { negativeButton() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        BottomSheetWith1Button(Modifier,"Connected To", "on 12544.154.545",
            negativeButton = { TextWithIcon(Icons.Default.Close, text = "Close")  },
            {})
    }
}