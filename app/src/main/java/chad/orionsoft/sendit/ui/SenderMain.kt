package chad.orionsoft.sendit.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.utils.NavigationConstants

@Preview
@Composable
fun SenderMainScreen(navTo: (String) -> Unit = {}) {

    Scaffold(topBar = {
        AppBarWithMessage(stringResource(R.string.app_name))
    }) {
        LazyVerticalGrid(modifier = Modifier.padding(it), columns = GridCells.Adaptive(100.dp)) {
            this.item {
                ActionItem(title = "Images", icon = R.drawable.image_icon_small) {
                    navTo(NavigationConstants.NAV_SENDER_IMAGES)
                }
            }
            this.item {
                ActionItem(title = "Audio", icon = R.drawable.music_icon_small) {}
            }
            this.item {
                ActionItem(title = "Video", icon = R.drawable.video_icon_small) {}
            }
            this.item {
                ActionItem(title = "Folder", icon = R.drawable.open_storage) {}
            }
            this.item {
                ActionItem(title = "Receive", icon = R.drawable.receive_icon_small) {}
            }
        }
    }

}

@Composable
private fun ActionItem(title:String, @DrawableRes icon: Int, navTo: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = navTo).padding(8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(painterResource(icon), contentDescription = null, Modifier.size(100.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
    }
}