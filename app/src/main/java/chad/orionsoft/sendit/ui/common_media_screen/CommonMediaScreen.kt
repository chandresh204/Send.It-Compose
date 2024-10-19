package chad.orionsoft.sendit.ui.common_media_screen

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.components.MediaItemAddGrid
import chad.orionsoft.sendit.ui.components.MediaItemViewBar
import chad.orionsoft.sendit.ui.components.MediaItemViewGrid
import chad.orionsoft.sendit.ui.components.NoPermissionView
import chad.orionsoft.sendit.utils.Converters

@Composable
fun CommonMediaTabContent(modifier: Modifier = Modifier,
                          mediaType: FileType,
                          navTo14Permission: () -> Unit,
                          onItemSelectedChanged: (MediaData, Boolean) -> Unit) {
    val vModel : CommonMediaScreenViewModel = hiltViewModel(
        creationCallback = { factory : CommonMediaScreenViewModel.CommonMediaViewModelFactory ->
            factory.create(mediaType)
        },
        key = mediaType.title
    )
    val context = LocalContext.current
    var isPermissionPermanentlyDenied by remember {
        mutableStateOf(false)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        Log.d("OnPermission", "$it")
        if(it.all { res -> !res.value }) {
            isPermissionPermanentlyDenied = true
        }
        vModel.loadMediaFromContentResolver(true)
    }
    val mediaList = vModel.mediaList
    val screenState by vModel.screenState.collectAsState()
    if(screenState.permissionType == PermissionType.NoPermission) {
        NoPermissionView(isDeniedPermanently = isPermissionPermanentlyDenied) {
            if(isPermissionPermanentlyDenied) {
                vModel.openAppSettingsToGetPermission()
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    navTo14Permission()
                } else {
                    permissionLauncher.launch(vModel.getRequiredPermissionString())
                }
            }
        }
        return
    }
    Column {
    when(screenState.viewMode) {
        ViewMode.ByNameInList -> {
            LazyColumn(modifier = modifier
                .weight(1f)
                .fillMaxSize()) {
                items(mediaList.size) { index ->
                    val media = mediaList[index]
                    MediaItemViewBar(
                        isSelected = media.selected,
                        thumbnail = media.thumbnailBitmap,
                        placeHolder = when(mediaType) {
                            FileType.FileTypeImage -> R.drawable.image_24px
                            FileType.FileTypeAudio -> R.drawable.music_note_24px
                            FileType.FileTypeVideo -> R.drawable.play_circle_24px
                            FileType.FileTypeOther -> R.drawable.file_present_24px
                        },
                        title = media.mediaData.title,
                        subtitle = "${Converters.formatDuration(media.mediaData.duration)} " +
                                Converters.formatDataString(media.mediaData.size),
                        actionIconVector = Icons.Default.PlayArrow,
                        actionIconDrawableRes = null,
                        onActionClick = {
                            StartActivity.openFile(context, media.mediaData.title, media.mediaData.uri)
                        }) {
                        onItemSelectedChanged(media.mediaData, !media.selected)
                        vModel.changeItemSelection(index)
                    }
                }
                if(screenState.permissionType == PermissionType.PartialAccess) {
                    item {
                        MediaItemAddGrid(onClick = navTo14Permission)
                    }
                }
                item {
                    LaunchedEffect(true) {
                        println("$mediaType >> loading more In List...")
                        vModel.loadMoreItemsToDisplay()
                    }
                }
            }
        }
        ViewMode.ByLastModifiedInGrid -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                items(mediaList.size) { index ->
                    val media = mediaList[index]
                    println("$mediaType >>> updating $index")
                    MediaItemViewGrid(
                        isSelected = media.selected,
                        thumbnail = media.thumbnailBitmap,
                        placeHolder = R.drawable.image_24px,
                        title = if (mediaType == FileType.FileTypeVideo) media.mediaData.title else "",
                        subtitle = Converters.formatDuration(media.mediaData.duration) +
                                "\n${Converters.formatDataString(media.mediaData.size)}",
                        actionIconVector = null,
                        actionIconDrawableRes = if (mediaType == FileType.FileTypeImage)
                            R.drawable.visibility_24px else R.drawable.play_arrow_24px,
                        onActionClick = {
                            StartActivity.openFile(
                                context,
                                media.mediaData.title,
                                media.mediaData.uri
                            )
                        }) {
                        vModel.changeItemSelection(index)
                        onItemSelectedChanged(media.mediaData, !media.selected)
                    }
                }
                if(screenState.permissionType == PermissionType.PartialAccess) {
                    item {
                        MediaItemAddGrid(onClick = navTo14Permission)
                    }
                }
                item {
                    LaunchedEffect(true) {
                        println("$mediaType >> loading more In Grid...")
                        vModel.loadMoreItemsToDisplay()
                    }
                }
            }

        }
        }
    }
}