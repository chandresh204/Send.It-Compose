package chad.orionsoft.sendit.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicator(count: Int, selected: Int) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.animateContentSize()) {
        for (i in 0 until count) {
            Indicator(isSelected = (selected == i))
        }
    }
}

@Composable
private fun Indicator(isSelected: Boolean) {
    Box(modifier = Modifier
        .size(
            if(isSelected) 25.dp else 20.dp
        )
        .padding(2.dp)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), CircleShape)
        .background(
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Preview
@Composable
private fun Preview() {
    PageIndicator(4,2)
}