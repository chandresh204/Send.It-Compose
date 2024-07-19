package chad.orionsoft.sendit.ui.receiver_main

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun ReceiverMainScreen(onBack: () -> Unit) {

 /*   val vModel = hiltViewModel<ReceiverMainViewModel>()
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
            if(it) {
                vModel.onEvent(ReceiverMainEvent.StartReceiver)
                isConnected = true
            }
            else onBack()
        }
    } */
}


@Composable
fun AnimatedLinearProgressBar(modifier: Modifier,
                              newProgress: () -> Float) {
    LinearProgressIndicator(progress = newProgress.invoke(),
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surface,
        modifier = modifier)
}