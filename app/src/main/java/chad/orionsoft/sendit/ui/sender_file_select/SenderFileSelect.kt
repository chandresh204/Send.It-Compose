package chad.orionsoft.sendit.ui.sender_file_select

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.ui.components.MediaItemViewBar
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Converters

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SenderFileSelect(
    fromSharedIntent: Intent?,
    onItemSelectedChanged: (MediaData, Boolean) -> Unit) {

    val vModel = hiltViewModel<FileSelectViewModel>()
    val uiState by vModel.fState.collectAsState()
    val context = LocalContext.current

    var consumedSharedIntent : Intent? by rememberSaveable {
        mutableStateOf(null)
    }

    val fileChooser = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    LaunchedEffect(fromSharedIntent) {
        fromSharedIntent?.let {
            if(fromSharedIntent != consumedSharedIntent) {
                vModel.onEvent(FileSelectEvent.OnFileData(it) { mData ->
                    onItemSelectedChanged(mData, true)
                })
                consumedSharedIntent = fromSharedIntent
            }
        }
    }

    val fileSelectLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.data == null) {
            Toast.makeText(context, "Data not found, please try again", Toast.LENGTH_LONG)
                .show()
        }
        result.data?.let {
            vModel.onEvent(FileSelectEvent.OnFileData(it) { mData ->
                onItemSelectedChanged(mData, true)
            })
        }
    }

    Scaffold {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(uiState.selectFiles, key = { mData ->
                mData.id
            }) {  mediaData ->
                MediaItemViewBar(
                    modifier = Modifier.animateItemPlacement(),
                    isSelected = false,
                    thumbUri = mediaData.thumbnail,
                    placeHolder = Converters.getPlaceHolderIconsAsPerMediaType(mediaData.mediaType),
                    title = mediaData.title,
                    subtitle = Converters.formatDataString(mediaData.size),
                    actionIconVector = Icons.Default.Close,
                    actionIconDrawableRes = null,
                    onActionClick = {
                        vModel.onEvent(FileSelectEvent.OnFileRemove(mediaData.id))
                        onItemSelectedChanged(mediaData, false)
                    }) {
                    StartActivity.openFile(context, mediaData.title, mediaData.uri)
                }
            }
            item {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        fileSelectLauncher.launch(Intent.createChooser(fileChooser, "Select Files"))
                    }) {
                        TextWithIcon(
                            painterResource(id = R.drawable.note_stack_add_24px),
                            text = "Add Files",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SenderFileSelectPrev() {
    SendItTheme {
        SenderFileSelect(null) { _,_  -> }
    }
}