package chad.orionsoft.sendit.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AsyncThumbnail
import coil.compose.AsyncImage

@Composable
fun FullScreenImage(path: String) {
    val fullUri = Uri.parse("content://media$path")
    AsyncImage(model = fullUri,
        modifier = Modifier.fillMaxSize(),
        contentDescription = null)
}