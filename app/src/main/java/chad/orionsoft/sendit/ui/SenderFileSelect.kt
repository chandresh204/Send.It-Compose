package chad.orionsoft.sendit.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.FileItemCard
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun SenderFileSelect() {
    Scaffold(topBar = {
        AppBarWithMessage(
            title = stringResource(id = R.string.app_name) + " - 122 Files",
            message = "12 Files selected, 34 MB",
            withProgress = 0.6f
        )
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(22) { index ->
                FileItemCard(
                    title = "Title : $index",
                    information = "Information: $index",
                    thumbnail = R.drawable.debian_icon_small,
                    isSelected = index %4 == 0,
                    isPlayIconElseViewIcon = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun SenderFileSelectPrev() {
    SendItTheme {
        SenderFileSelect()
    }
}