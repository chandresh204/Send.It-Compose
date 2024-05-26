package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import chad.orionsoft.sendit.utils.Constants

@Composable
fun FileItemCard(
    title: String,
    information: String,
    thumbnail: Int,
    isSelected: Boolean,
    isPlayIconElseViewIcon: Boolean
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = Constants.PAGE_PADDING.dp,
            vertical = 6.dp
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.background
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)) {
            Image(
                painter = painterResource(thumbnail),
                modifier= Modifier.size(40.dp),
                contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = information,
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {  }) {
                if(isPlayIconElseViewIcon) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "play")
                } else {
                    Icon(painterResource(R.drawable.eye_icon_small), contentDescription = "view")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FileItemCardPrev() {
    Column {
        FileItemCard(
            title = "Audio or Video Item Selected",
            information = "0:33 25.5 MB",
            thumbnail = R.drawable.music_icon_small,
            isSelected = true,
            isPlayIconElseViewIcon = true)
        FileItemCard(isSelected = false,
            title = "Audio or Video Item",
            information = "0:33 25.5 MB",
            thumbnail = R.drawable.video_icon_small,
            isPlayIconElseViewIcon = true)
        FileItemCard(isSelected = false,
            title = "Any file",
            information = "25.5 MB",
            thumbnail = R.drawable.file_unknown_icon_small,
            isPlayIconElseViewIcon = false)
    }
}