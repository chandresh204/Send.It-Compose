package chad.orionsoft.sendit.ui.sender_file_select

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun SenderFileSelect() {

    val vModel = hiltViewModel<FileSelectViewModel>()
    val uiState by vModel.fState.collectAsState()

    Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - 122 Files",
            message = "12 Files selected, 34 MB",
            withProgress = 0.6f
        )
    },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vModel.onEvent(FileSelectEvent.OnAddNewClicked)
            }) {
                Icon(Icons.Default.Add, contentDescription = "add")
            }
        }) {
        if(uiState.selectFiles.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.add_files_manually),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(22) { index ->
//                    FileItemCard(
//                        title = "Title : $index",
//                        information = "Information: $index",
//                        thumbnail = R.drawable.debian_icon_small,
//                        isSelected = index %4 == 0,
//                        isPlayIconElseViewIcon = false
//                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SenderFileSelectPrev() {
    SendItTheme {
        SenderFileSelect()
    }
}