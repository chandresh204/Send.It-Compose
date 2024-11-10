package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R

@Composable
fun SendArrow(modifier: Modifier = Modifier,size: Int = 30, rotation : Float = -90f) {
    Box(modifier = modifier.rotate(rotation)) {
        Image(ImageVector.vectorResource(R.drawable.arrow_flat),
            modifier = Modifier.size(size.dp),
            contentDescription = null)
        Box(modifier = Modifier.size((size/5).dp)
            .align(Alignment.CenterStart)
            .offset(x = (size/5).dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendArrow(size = 40)
}