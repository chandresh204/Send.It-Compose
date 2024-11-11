package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun AppBarWithProgress(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    infoString : String,
    speedCount: Float,
    speedUnit : String,
    itemCount : String?
    ) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Clear, contentDescription = "Cancel")
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if(infoString.isNotEmpty()) Text(text = infoString)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 8.dp)
                ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = speedCount.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text(text = speedUnit,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 2.dp))
                }
                itemCount?.let {
                    Text(text = it,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.5f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        AppBarWithProgress(
            infoString = "Connected to Vivo",
            speedUnit = "GB/s",
            speedCount = 2.5f,
            itemCount = "11/100"
        )
    }
}