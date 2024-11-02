package chad.orionsoft.sendit.ui.welcome_screen.welcome_pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppColorSetting
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.Constants

@Composable
fun WelcomePage2(modifier: Modifier = Modifier,
                 allColors: List<ColorConstants>,
                 selectedColor: ColorConstants,
                 onSaveColor: () -> Unit,
                 onColorSelected: (ColorConstants) -> Unit) {
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Constants.PAGE_PADDING.dp)) {
        Text(text = stringResource(R.string.setting_color_title), style = MaterialTheme.typography.titleLarge)
        AppColorSetting(availableColors = allColors,
            selectedColor = selectedColor,
            onColorSelected = onColorSelected)
        Button(onClick = onSaveColor) {
            Text(text = stringResource(R.string.save_and_continue))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
//    WelcomePage2()
}