package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun OptionSelectBottomSheet(
    title: String
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        // TODO finish this
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        OptionSelectBottomSheet("Select Sort Order")
    }
}

