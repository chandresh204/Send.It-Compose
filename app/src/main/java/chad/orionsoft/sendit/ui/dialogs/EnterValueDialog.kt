package chad.orionsoft.sendit.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants

@Composable
fun EnterValueDialog(
    title: String,
    subtitle: String,
    onCodeSubmit: (String) -> Unit,
    onDismiss: () -> Unit) {
    var codeStr by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        )
    ) {
        Card(modifier = Modifier
            .fillMaxWidth(),
            shape = RoundedCornerShape(Constants.DIALOG_CORNER.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(Constants.PAGE_PADDING.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = codeStr,
                    shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp),
                    onValueChange = {
                        codeStr = it
                    },
                    textStyle = TextStyle(
                        fontSize = 35.sp,
                        letterSpacing = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                )
                Button(onClick = {
                    onCodeSubmit(codeStr)
                }) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SendItTheme {
        EnterValueDialog("Add Code", "to Connect", {}) {}
    }
}