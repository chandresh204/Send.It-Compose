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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R

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
                text = stringResource(R.string.permission_not_granted),
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = stringResource(R.string.request_permission_text),
                textAlign = TextAlign.Center)
            Button(onClick = onAllowClick) {
                Text(text = if(isDeniedPermanently) stringResource(R.string.goto_settings)
                else stringResource(R.string.allow_access))
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    NoPermissionView(isDeniedPermanently = true) {}
}