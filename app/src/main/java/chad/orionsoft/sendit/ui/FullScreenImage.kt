package chad.orionsoft.sendit.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage

@Composable
fun FullScreenImage(uri: Uri, onBack: () -> Unit) {
    Box {
        AsyncImage(model = uri,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentDescription = null)
        IconButton(onClick = onBack,
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background.copy(
                        alpha = 0.5f
                    )).size(50.dp)
                    .padding(8.dp),
                contentDescription = "Back")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FullScreenImagePrev() {
    FullScreenImage(":".toUri()) { }
}