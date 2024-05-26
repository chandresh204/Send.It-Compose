package chad.orionsoft.sendit.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.FileItemCard

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