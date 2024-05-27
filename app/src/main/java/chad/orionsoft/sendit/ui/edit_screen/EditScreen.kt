package chad.orionsoft.sendit.ui.edit_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(onBack: () -> Unit) {

    val viewModel = hiltViewModel<EditScreenViewModel>()
    val uiState by viewModel.editScreenState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name) +
                        " - " + stringResource(id = R.string.edit))
            },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                })
        }
    ) {
        Column(modifier = Modifier
            .padding(it)
            .padding(horizontal = Constants.PAGE_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(Constants.PAGE_PADDING.dp)
        ) {
            Text(text = "Username:")
            OutlinedTextField(
                value = uiState.userName, onValueChange = { new ->
                    viewModel.onEvent(EditScreenEvent.OnUseNameUpdated(new))
                },
                shape = RoundedCornerShape(
                    Constants.OUTLINE_TEXT_FIELD_RADIUS.dp
                )
            )
    //        MaxImageSizeToPreviewSettings() // TODO remove if not required
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    viewModel.onEvent(EditScreenEvent.OnSaveClicked)
                    onBack()
                }, enabled = uiState.userName.isNotEmpty()) {
                    TextWithIcon(Icons.Default.Check, text = stringResource(R.string.save))
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = onBack) {
                    TextWithIcon(Icons.Default.Close, text = stringResource(R.string.discard))
                }
            }
            Text(text = "V XX",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center) // TODO replace with BuildConfig
        }
    }
}

@Composable
fun MaxImageSizeToPreviewSettings() {
    Row {
        Text(text = "Maximum Image Size Allowed In MB (in preview)")
    }
}