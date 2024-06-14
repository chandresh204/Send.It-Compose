package chad.orionsoft.sendit.ui.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants

@Composable
fun MediaItemViewBar(
    isSelected : Boolean,
    thumbUri: Uri,
    @DrawableRes placeHolder: Int,
    title: String,
    subtitle: String,
    actionIconVector: ImageVector?,
    @DrawableRes actionIconDrawableRes: Int?,
    onActionClick: () -> Unit,
    onViewClick:() -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = Constants.PAGE_PADDING.dp,
            vertical = 6.dp
        )
        .clickable(onClick = onViewClick),
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
            AsyncThumbnail(placeholder = placeHolder, imageUri = thumbUri,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = subtitle,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onActionClick) {
                if(actionIconVector != null) {
                    Icon(actionIconVector, contentDescription = "action")
                } else if (actionIconDrawableRes != null){
                    Icon(painterResource(actionIconDrawableRes),
                        contentDescription = "action")
                }
            }
        }
    }
}

@Composable
fun MediaItemViewGrid(
    isSelected : Boolean,
    thumbUri: Uri,
    @DrawableRes placeHolder: Int?,
    title: String,
    subtitle: String,
    actionIconVector: ImageVector?,
    @DrawableRes actionIconDrawableRes: Int?,
    onActionClick: () -> Unit,
    onViewClick:() -> Unit
) {
    Column(modifier = Modifier.padding(4.dp)) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {
            Box(modifier = Modifier) {
                AsyncThumbnail(placeholder = placeHolder,
                    imageUri = thumbUri,
                    thumbnailSize = 300,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isSelected) 0.5f else 1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewClick))
                IconButton(onClick = onActionClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(
                            32.dp
                        )) {
                    if(actionIconVector != null) {
                        Image(
                            actionIconVector,
                            contentDescription = "action")
                    } else if (actionIconDrawableRes != null) {
                        Image(
                            painterResource(id = actionIconDrawableRes),
                            contentDescription = "action")
                    }
                }
                if(title.isNotEmpty()) {
                    Text(text = subtitle,
                        modifier = Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(5.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.background,
                                offset = Offset(2f,2f),
                            )
                        ))
                }
                if(isSelected) {
                    Checkbox(checked = true,
                        modifier = Modifier.align(Alignment.BottomStart),
                        onCheckedChange = {})
                }
            }
        }
        Text(text = title, maxLines = 1, style = MaterialTheme.typography.bodySmall)
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        Column {
            MediaItemViewBar(
                isSelected = false,
                thumbUri = Uri.parse(""),
                placeHolder = R.drawable.music_icon_small,
                title = "Music 1",
                subtitle = "Size 12 MB, 00:00",
                actionIconVector = null,
                actionIconDrawableRes = R.drawable.eye_icon_small,
                onActionClick = {}
            ) {}
            MediaItemViewBar(
                isSelected = true,
                thumbUri = Uri.parse(""),
                placeHolder = R.drawable.music_icon_small,
                title = "Music 2",
                subtitle = "Size 21 MB, 00:14",
                actionIconVector = Icons.Default.Check,
                actionIconDrawableRes = null,
                onActionClick = {}
            ) {}
            Row {
                MediaItemViewGrid(
                    isSelected = true,
                    thumbUri = Uri.parse(""),
                    placeHolder = R.drawable.music_icon_small,
                    title = "Music 2",
                    subtitle = "00:14",
                    actionIconVector = Icons.Default.Check,
                    actionIconDrawableRes = null,
                    onActionClick = {}
                ) {}
                MediaItemViewGrid(
                    isSelected = false,
                    thumbUri = Uri.parse(""),
                    placeHolder = R.drawable.music_icon_small,
                    title = "",
                    subtitle = "05:30",
                    actionIconVector = null,
                    actionIconDrawableRes = R.drawable.eye_icon_small,
                    onActionClick = {}
                ) {}
            }
        }

    }
}