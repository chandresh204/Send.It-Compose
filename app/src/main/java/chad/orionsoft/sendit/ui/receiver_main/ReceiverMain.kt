package chad.orionsoft.sendit.ui.receiver_main

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import chad.orionsoft.sendit.ui.components.AppBarWithProgress
import chad.orionsoft.sendit.ui.components.EmptyView
import chad.orionsoft.sendit.ui.components.InProgressItemCard
import chad.orionsoft.sendit.ui.components.SteadyItemCard
import chad.orionsoft.sendit.ui.create_connection_flow.ui.CreateConnectionReceiver
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.RuntimeConstants

@Composable
fun ReceiverMainScreen(onBack: () -> Unit) {

    val vModel = hiltViewModel<ReceiverMainViewModel>()
    val uiState by vModel.receiverState.collectAsState()
    
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
    Scaffold {
        AppBarWithProgress(Modifier.padding(it),
            infoString = if(isConnected) "Connected to: ${RuntimeConstants.connectedDeviceDetails?.name}" else "",
            speedCount = 0f, speedUnit = "B/s", itemCount = null)
        if(uiState.receivedList.isEmpty() && uiState.inProgress==null) {
            EmptyView(stringResource(R.string.received_here))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(it)
                .padding(horizontal = Constants.PAGE_PADDING.dp)) {
            uiState.inProgress?.let {
                item {
                    InProgressItemCard(
                        icon = it.icon,
                        title = it.title,
                        subTitle = it.infoString,
                        progressInFloat = it.progressState
                    )
                }
            }
            items(uiState.receivedList) { item ->
                SteadyItemCard(icon = R.drawable.music_icon,
                    title = item.title, size = item.infoString)
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
fun AnimatedLinearProgressBar(modifier: Modifier,
                              newProgress: () -> Float) {
    LinearProgressIndicator(progress = newProgress.invoke(),
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surface,
        modifier = modifier)
}

