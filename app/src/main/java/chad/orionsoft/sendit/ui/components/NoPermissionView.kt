package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoPermissionView(modifier: Modifier = Modifier,
                     isDeniedPermanently : Boolean,
                     onAllowClick: () -> Unit) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Permission Not Granted",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Send.It can not access media files if the permission is not granted," +
                    "click on the allow button to provide permission",
                textAlign = TextAlign.Center)
            Button(onClick = onAllowClick) {
                Text(text = if(isDeniedPermanently) "Go To Settings" else "Allow Access")
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    NoPermissionView(isDeniedPermanently = true) {}
}