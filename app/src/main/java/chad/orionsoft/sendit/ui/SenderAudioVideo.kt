package chad.orionsoft.sendit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.FileItemCard
import chad.orionsoft.sendit.utils.Constants

@Preview
@Composable
fun SenderAudioVideo() {
    Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - 122 Files",
            message = "12 Files selected, 34 MB",
            withProgress = 0.6f
        )
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(22) { index ->
                FileItemCard(
                    title = "title : $index",
                    information = "0:$index $index MB",
                    thumbnail = R.drawable.music_icon_small,
                    isSelected = index % 4 == 0,
                    isPlayIconElseViewIcon = true
                )
            }
        }
    }
}