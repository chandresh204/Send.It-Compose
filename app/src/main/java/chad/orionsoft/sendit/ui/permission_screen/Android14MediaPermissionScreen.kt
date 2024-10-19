package chad.orionsoft.sendit.ui.permission_screen

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith1Button
import chad.orionsoft.sendit.ui.common_media_screen.PermissionType
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.utils.Constants

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android14MediaPermissionScreen(
    vModel : Android14PermissionViewModel = hiltViewModel(),
    backToParentScreen: () -> Unit
) {
    val pState by vModel.pState.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vModel.onEvent(PermissionEvents.OnPermissionObtained(it))
    }

    // TODO refresh counts on resume after returning from app settings

    var openAppSettingsRequested by remember {
        mutableStateOf(false)
    }

    if(openAppSettingsRequested) {
        BottomSheetWith1Button(
            title = "Go To App Settings",
            subtitle = "Go to App Settings",
            negativeButton = { Text(text = "Open App Settings") }) {
            openAppSettingsRequested = false
            vModel.onEvent(PermissionEvents.OpenAppSettings)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.app_name))
        })
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(
                    Constants.PAGE_PADDING.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Android 14 permission explaining text")
            if(!pState.isAudioPermission || pState.avPermissionType == PermissionType.NoPermission) {
                OutlinedButton(onClick = {
                    permissionLauncher.launch(
                        vModel.getAllRequiredPermissionArray()
                    )
                }) {
                    Text(text = "Allow access")
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        permissionLauncher.launch(
                            vModel.getAllRequiredPermissionArray()
                        )
                    }) {
                        Text(text = "Allow access to more")
                    }
                    Button(onClick = {
                        openAppSettingsRequested = true
                    }) {
                        Text(text = "Allow All")
                    }
                }
            }
            if (pState.isAudioPermission && pState.avPermissionType != PermissionType.NoPermission) {
                MediaCountRow(
                    iconRes = R.drawable.image_24px,
                    count = pState.imageAccessedCount,
                    title = "Image(s)"
                )
                MediaCountRow(
                    iconRes = R.drawable.music_note_24px,
                    count = pState.audioAccessCount,
                    title = "Audio"
                )
                MediaCountRow(
                    iconRes = R.drawable.play_circle_24px,
                    count = pState.videoAccessCount,
                    title = "Video(s)"
                )
                Button(onClick = backToParentScreen) {
                    TextWithIcon(
                        Icons.Default.Check,
                        text = "Done"
                    )
                }
            }
        }
    }
}

@Composable
fun MediaCountRow(modifier: Modifier = Modifier, iconRes : Int, count: Int, title: String) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            modifier = Modifier.size(44.dp),
            imageVector = ImageVector.vectorResource(id = iconRes),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "image")
        Text(text = count.toString(), style = MaterialTheme.typography.titleLarge)
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Icon(Icons.Default.Check, "Checked")
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    MediaCountRow(iconRes = R.drawable.image_24px, count = 23, title = "Images")
}