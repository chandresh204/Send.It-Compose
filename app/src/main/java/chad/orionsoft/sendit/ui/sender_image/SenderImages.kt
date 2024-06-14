package chad.orionsoft.sendit.ui.sender_image

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.DropDownMenuData
import chad.orionsoft.sendit.ui.components.MediaItemViewGrid

@Composable
fun SenderImagesScreen(viewFullScreen: (path: String) -> Unit, onBack: () -> Unit) {

    val vModel = hiltViewModel<SenderImagesViewModel>()
    val uiState by vModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    Scaffold(
        topBar = {
            AppBarWithMessage(
                title = stringResource(R.string.app_name) + " - ${uiState.images.size} Images",
                message = "${uiState.selectedCount} selected, Total : ${uiState.selectedSizeToDisplay}, Free Space: 1.1 GB",
                withProgress = 0.2f,
                onBackPress = onBack,
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
                        viewFullScreen(img.imageData.uri.path ?: "")
                    }) {
                    vModel.onEvent(SenderImagesEvent.OnSelectClick(img.imageData.id))
                }
            }
        }
    }
}