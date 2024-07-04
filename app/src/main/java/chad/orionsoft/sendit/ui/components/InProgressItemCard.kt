package chad.orionsoft.sendit.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.receiver_main.AnimatedLinearProgressBar
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun InProgressItemCard(
    @DrawableRes icon: Int,
    title:String,
    subTitle: String,
    progressInFloat: Float
) {
    Card(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Image(
                painterResource(icon),
                modifier = Modifier.size(40.dp),
                contentDescription = null)
            Column(
                Modifier.height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(text = subTitle,
                    style = MaterialTheme.typography.bodySmall)
                AnimatedLinearProgressBar(modifier = Modifier
                    .fillMaxWidth()) {
                    progressInFloat
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SendItTheme {
        InProgressItemCard(
            icon = R.drawable.debian_icon_small,
            title = "debian 15",
            subTitle = "12M/123M",
            progressInFloat = 0.5f)
    }
}