package chad.orionsoft.sendit.ui.sender_image

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
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
            it.imageData.id
        }) { img ->
            MediaItemViewGrid(
                isSelected = img.isSelected,
                thumbUri = img.imageData.uri,
                placeHolder = null,
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

  /*  Scaffold(
        topBar = {
            AppBarWithMessage(
                title = stringResource(R.string.app_name) + " - ${uiState.images.size} Images",
                message = "${uiState.selectedCount} selected, Total : ${uiState.selectedSizeToDisplay}, Free Space: 1.1 GB",
                withProgress = 0.2f,
                appBarMenu = listOf(
                    DropDownMenuData(Icons.Default.Check, "Select all") {
                        vModel.onEvent(SenderImagesEvent.OnSelectAll)
                    },
                    DropDownMenuData(Icons.Default.Clear, "Clear all") {
                        vModel.onEvent(SenderImagesEvent.OnClearAllSelection)
                    },
                )
            )
        }
    ) {
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.padding(it),
            columns = GridCells.Adaptive(100.dp)) {
            items(uiState.images, key = {
                it.imageData.id
            }) { img ->
                MediaItemViewGrid(
                    isSelected = img.isSelected,
                    thumbUri = img.imageData.uri,
                    placeHolder = null,
                    title = "",
                    subtitle = "",
                    actionIconVector = null,
                    actionIconDrawableRes = R.drawable.eye_icon_small,
                    onActionClick = {
                        viewFullScreen(Converters.encodeUriPath(img.imageData.uri.path.toString()))
                    }) {
                    vModel.onEvent(SenderImagesEvent.OnSelectClick(img.imageData.id, onItemSelecteChanged))
                }
            }
        }
    } */

}