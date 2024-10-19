package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chad.orionsoft.sendit.utils.isLightColor

@Composable
fun CharInCircle(modifier: Modifier = Modifier, char: Char, color: Color, size: Int) {
    Box(modifier = modifier.size(size.dp)
        .clip(CircleShape)
        .background(color)) {
        Text(text = char.toString(),
            modifier = Modifier.align(Alignment.Center),
            color = if(color.isLightColor()) Color.Black else Color.White,
            fontSize = size.sp / 1.5
            )
    }
}

@Preview
@Composable
private fun Preview() {
    CharInCircle(
        char = '0',
        color = Color.Gray,
        size = 90
    )
}