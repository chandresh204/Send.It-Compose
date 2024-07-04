package chad.orionsoft.sendit.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun EmptyView(msg: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = msg, style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.Center))
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    SendItTheme {
        EmptyView("Nothing here")
    }
}