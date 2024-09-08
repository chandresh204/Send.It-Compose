package chad.orionsoft.sendit.ui.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun SteadyItemCard(
    @DrawableRes thumbnailPlaceholder: Int,
    title: String,
    size: String,
    thumbnail: Uri?,
    progressStatus: ProgressStatus? = null
) {
    Card(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Box {
                AsyncThumbnail(
                    placeholder = thumbnailPlaceholder,
                    imageUri = thumbnail ?: Uri.parse(""),
                    modifier = Modifier.size(50.dp).clip(CircleShape))
                progressStatus?.let {
                    val icon = when(it) {
                        ProgressStatus.Sending -> Icons.Default.KeyboardArrowUp
                        ProgressStatus.Receiving -> Icons.Default.KeyboardArrowDown
                        ProgressStatus.Success -> Icons.Default.Check
                        ProgressStatus.Failed -> Icons.Default.Warning
                        else -> Icons.Default.MoreVert
                    }
                    Icon(
                        icon,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(color = MaterialTheme.colorScheme.background.copy(
                                alpha = 0.5f
                            ))
                            .padding(8.dp),
                        contentDescription = null
                    )
                }
            }
            Column(
                Modifier
                    .height(IntrinsicSize.Max)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(text = size,
                    style = MaterialTheme.typography.bodySmall)
            }
            if(progressStatus == ProgressStatus.Sending) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Clear, contentDescription = "remove")
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SendItTheme {
        SteadyItemCard(thumbnailPlaceholder = R.drawable.music_icon,
            title = "Cool Weather", size = "11 MB",
            thumbnail = Uri.parse(""),
            progressStatus = ProgressStatus.Success)
    }
}