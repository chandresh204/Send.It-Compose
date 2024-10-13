package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.utils.Constants

@Composable
fun UserNameTextField(modifier: Modifier = Modifier, myName: String, onNameEdited: (String) -> Unit) {
    Column {
        Text(text = "Your Name", style = MaterialTheme.typography.titleMedium)
        Text(text = "This will appear on other devices", fontStyle = FontStyle.Italic)
        OutlinedTextField(
            value = myName,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Face, contentDescription = "User")
            },
            placeholder = {
                Text(text = "e.g. John",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            },
            label = {
                Text(text = "My Name")
            },
            shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            onValueChange = onNameEdited
        )
    }
}