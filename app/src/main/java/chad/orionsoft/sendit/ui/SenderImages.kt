package chad.orionsoft.sendit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage

@Preview
@Composable
fun SenderImagesScreen(viewFullScreen: (path: String) -> Unit = {}) {
    Scaffold(
        topBar = {
            AppBarWithMessage(
                title = stringResource(R.string.app_name) + " - 20 Images",
                message = "20 selected, Total : 20 MB, Free Space: 1.1 GB",
                withProgress = 0.2f
            )
        }
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(it),
            columns = GridCells.Adaptive(100.dp)) {
            items(20) {
                ImageView(it%5==0) {
                    viewFullScreen("")
                }
            }
        }
    }
}

@Composable
private fun ImageView(isSelected: Boolean, viewFullScreen: () -> Unit) {
    Box(modifier = Modifier) {
        Image(
            painterResource(id = R.drawable.sendit_icon_graphics),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .alpha(if (isSelected) 0.5f else 1f)
                .height(120.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentDescription = null
        )
        IconButton(onClick = viewFullScreen,
            modifier = Modifier
            .align(Alignment.TopEnd)) {
            Image(
                painterResource(R.drawable.eye_icon_small),
                contentDescription = "view")
        }
        if(isSelected) {
            Checkbox(checked = true,
                modifier = Modifier.align(Alignment.BottomStart),
                onCheckedChange = {})
        }
    }
}