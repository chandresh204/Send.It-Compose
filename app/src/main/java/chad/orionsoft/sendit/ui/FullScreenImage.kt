package chad.orionsoft.sendit.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

@Composable
fun FullScreenImage(path: String) {
    val fullUri = Uri.parse("content://media$path")
    AsyncImage(model = fullUri,
        modifier = Modifier.fillMaxSize(),
        contentDescription = null)
}