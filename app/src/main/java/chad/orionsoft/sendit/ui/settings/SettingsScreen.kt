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
import chad.orionsoft.sendit.ui.theme.ColorBlue
import chad.orionsoft.sendit.ui.theme.ColorGreen
import chad.orionsoft.sendit.ui.theme.ColorRed
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
            Text(text = "Your Name", style = MaterialTheme.typography.titleMedium)
            Text(text = "This will appear on other devices", fontStyle = FontStyle.Italic)
            OutlinedTextField(
                value = uiState.myName,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Face, contentDescription = "User")
                },
                placeholder = {
                    Text(text = "e.g. John",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                },
                label = {
                    Text(text = "My Name")
                },
                shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                onValueChange = {
                    vModel.onEvent(SettingsEvents.OnNameEdited(it))
                }
            )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppColorSetting(modifier: Modifier = Modifier,
                            availableColors: List<ColorConstants>,
                            selectedColor: ColorConstants,
                            onColorSelected : (ColorConstants) -> Unit) {
    Column(modifier) {
        Text(text = "Your App Color", style = MaterialTheme.typography.titleMedium)
        Text(text = "This is the primary color of your app, " +
                "but connected device will also show your color in some UI elements",
            fontStyle = FontStyle.Italic)
        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
            availableColors.forEach {
                ColorPlate(color=it.color, isSelected = it == selectedColor,
                    isSystem = it == ColorConstants.Device, onClick = { onColorSelected(it) })
            }
        }
    }
}

@Composable
private fun ColorPlate(modifier: Modifier = Modifier,
                       color: Color?,
                       isSelected: Boolean,
                       isSystem: Boolean,
                       onClick: () -> Unit
) {
    Box(modifier = modifier
        .size(100.dp)
        .padding(4.dp)
        .clip(CircleShape)
        .background(color ?: Color.Gray)
        .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(isSystem) {
                Text(text = "Device",
                    color = MaterialTheme.colorScheme.onPrimary)
            }
            if(isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "Select")
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