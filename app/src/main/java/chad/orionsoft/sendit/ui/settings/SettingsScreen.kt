package chad.orionsoft.sendit.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.ui.components.AppColorSetting
import chad.orionsoft.sendit.ui.components.UserNameTextField
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier,
                   themeChangeCallBack: (ColorConstants) -> Unit,
                   vModel : SettingsViewModel = hiltViewModel(),
                   onBack: () -> Unit) {
    val uiState by vModel.uiState.collectAsState()
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "back")
                    }
                })
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(Constants.PAGE_PADDING.dp)
            .verticalScroll(verticalScrollState)) {
            UserNameTextField(myName = uiState.myName) {
                vModel.onEvent(SettingsEvents.OnNameEdited(it))
            }
            Spacer(modifier = Modifier.padding(16.dp))
            AppColorSetting(availableColors = uiState.appColorList,
                selectedColor = uiState.appColor) { appColor ->
                vModel.onEvent(SettingsEvents.OnAppColorSelected(appColor))
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Button(onClick = {
                vModel.onEvent(SettingsEvents.OnSettingSave)
                themeChangeCallBack(uiState.appColor)
                onBack()
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text = "Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppColorSetting(availableColors = ColorConstants.entries, selectedColor = ColorConstants.Red) {

    }
}