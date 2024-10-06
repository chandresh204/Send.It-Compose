package chad.orionsoft.sendit.ui.sender_image

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.ContentResolverDomain
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.components.MediaItemViewGrid

@Composable
fun SenderImagesScreen(onItemSelectedChanged: (MediaData, Boolean) -> Unit) {

    val vModel = hiltViewModel<SenderImagesViewModel>()
    val uiState by vModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(100.dp)) {
        items(uiState.images, key = {
            "${it.imageData.id}-${it.thumbnailBitmap}"
        }) { img ->
            MediaItemViewGrid(
                isSelected = img.isSelected,
                thumbnail = img.thumbnailBitmap,
                placeHolder = R.drawable.image_24px,
                title = "",
                subtitle = "",
                actionIconVector = null,
                actionIconDrawableRes = R.drawable.visibility_24px,
                onActionClick = {
                    StartActivity.openFile(context, img.imageData.title, img.imageData.uri)
                }) {
                onItemSelectedChanged(img.imageData, !img.isSelected)
                vModel.onEvent(SenderImagesEvent.OnSelectClick(img.imageData.id))
            }
        }
    }
}