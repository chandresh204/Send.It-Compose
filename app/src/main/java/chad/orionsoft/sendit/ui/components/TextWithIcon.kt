package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextWithIcon(imageVector: ImageVector,
                 text: String,
                 textStyle: TextStyle = TextStyle.Default,
                 modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
        Text(text = text, style = textStyle)
    }
}

@Preview(showBackground = true)
@Composable
private fun TextWithIconPreview() {
    TextWithIcon(Icons.Default.Add ,"Add")
}