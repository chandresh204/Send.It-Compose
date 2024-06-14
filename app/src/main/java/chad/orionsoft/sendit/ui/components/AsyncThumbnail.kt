package chad.orionsoft.sendit.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun AsyncThumbnail(modifier: Modifier = Modifier,
                   @DrawableRes placeholder: Int?,
                   imageUri: Uri,
                   thumbnailSize : Int = 100) {

    var loadedBitmap : Bitmap? by remember {
        mutableStateOf(null)
    }
    val context = LocalContext.current
    LaunchedEffect(imageUri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadedBitmap = context.contentResolver.loadThumbnail(
                    imageUri, Size(thumbnailSize,thumbnailSize), null)
            }
            Log.d("AsyncThumbnail", "loaded for: $imageUri")
        } catch (e: Exception) {
            Log.e("AsyncThumbnail", "$e")
        }

    }

    if(loadedBitmap == null) {
        if(placeholder == null) {
            Box(modifier = modifier)
        } else {
            Image(
                painterResource(id = placeholder),
                contentScale = ContentScale.Crop,
                modifier = modifier,
                contentDescription = null
            )
        }
    } else {
        Image(
            loadedBitmap!!.asImageBitmap(),
            contentScale = ContentScale.Crop,
            modifier= modifier,
            contentDescription = null
        )
    }


}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        AsyncThumbnail(modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
            placeholder = R.drawable.music_icon_small,
        Uri.parse("")
        )
    }
}