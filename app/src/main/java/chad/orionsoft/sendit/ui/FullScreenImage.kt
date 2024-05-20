package chad.orionsoft.sendit.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R

@Preview
@Composable
fun FullScreenImage() {
    Image(painterResource(id = R.drawable.sendit_icon_graphics),
        contentDescription = null)
}