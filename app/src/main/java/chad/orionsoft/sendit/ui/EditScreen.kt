package chad.orionsoft.sendit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name) +
                        " - " + stringResource(id = R.string.edit))
            },
                navigationIcon = {
                    IconButton(onClick = {  }) {
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
                value = "", onValueChange = { },
                shape = RoundedCornerShape(
                    Constants.OUTLINE_TEXT_FIELD_RADIUS.dp
                )
            )
            MaxImageSizeToPreviewSettings() // TODO remove if not required
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) {
                    TextWithIcon(Icons.Default.Check, text = stringResource(R.string.save))
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = { }) {
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