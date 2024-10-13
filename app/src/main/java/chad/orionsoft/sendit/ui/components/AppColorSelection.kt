package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.utils.ColorConstants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppColorSetting(modifier: Modifier = Modifier,
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