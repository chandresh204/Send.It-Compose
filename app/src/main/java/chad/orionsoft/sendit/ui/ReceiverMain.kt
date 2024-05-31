package chad.orionsoft.sendit.ui

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.create_connection_flow.ui.CreateConnectionReceiver
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants

@Composable
fun ReceiverMainScreen(onBack: () -> Unit) {
    var appBarBriefProgress by remember {
        mutableStateOf("")
    }
    var isConnected by remember {
        mutableStateOf(false)
    }
    val listState = rememberLazyListState()
    val listFirstItemIndex = remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    if(listFirstItemIndex.value > 0) {
        Log.d("FirstIndex", "${listFirstItemIndex.value}")
        appBarBriefProgress = "current progress will be shown here"
    } else appBarBriefProgress = ""
    Scaffold(
        topBar = {
            AppBarWithMessage(title = stringResource(id = R.string.app_name),
                appBarBriefProgress, withProgress = 0.1f)
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(it)
                .padding(horizontal = Constants.PAGE_PADDING.dp)) {
            item {
                InProgressCard(
                    icon = R.drawable.debian_icon,
                    title = "Debian.iso",
                    progressByTotal = "123M/1.2G",
                    progressInFloat = 0.2f)
            }
            items(20) { pos ->
                InQueueCard(icon = R.drawable.music_icon,
                    title = "Item $pos", size = "$pos MB")
            }
        }
    }
    if(!isConnected) {
        CreateConnectionReceiver {
            if(it) isConnected = true
            else onBack()
        }
    }
}

@Composable
fun InProgressCard(
    @DrawableRes icon: Int,
    title:String,
    progressByTotal: String,
    progressInFloat: Float
    ) {
    Card(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Image(painterResource(icon),
                modifier = Modifier.size(40.dp),
                contentDescription = null)
            Column(Modifier.height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(text = progressByTotal,
                    style = MaterialTheme.typography.bodySmall)
                AnimatedLinearProgressBar(modifier = Modifier
                    .fillMaxWidth()) {
                    progressInFloat
                }
            }
        }
    }
}

@Composable
fun InQueueCard(
    @DrawableRes icon: Int,
    title: String,
    size: String
) {
    Card(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Image(painterResource(icon),
                modifier = Modifier.size(40.dp),
                contentDescription = null)
            Column(Modifier.height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(text = size,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AnimatedLinearProgressBar(modifier: Modifier,
                              newProgress: () -> Float) {
    LinearProgressIndicator(progress = newProgress.invoke(),
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surface,
        modifier = modifier)
}

@Preview
@Composable
private fun InProgressCardPreview() {
    SendItTheme {
        InProgressCard(
            R.drawable.debian_icon, "casd.tar.gz",
            "12MB/12GB", 0.05f
        )
    }
}
