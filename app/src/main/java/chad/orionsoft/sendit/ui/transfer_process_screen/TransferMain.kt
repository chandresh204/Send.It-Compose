package chad.orionsoft.sendit.ui.transfer_process_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.repository.ConnectProcessRepository
import chad.orionsoft.sendit.ui.components.SendArrow
import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferTab
import chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving.TransferReceivingScreen
import chad.orionsoft.sendit.ui.transfer_process_screen.transfer_sending.TransferSendingScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMain(landToReceivingTab:Boolean, onBack: () -> Unit) {
    val vModel = hiltViewModel<TransferMainViewModel>()
    val uiState by vModel.tMainState.collectAsState()
    val pagerState = rememberPagerState {
        2
    }
    LaunchedEffect(Unit) {
        ConnectProcessRepository.connectionUpdateReceiver.collectLatest {
            if(it == null) {
                onBack()
            }
        }
    }
    LaunchedEffect(landToReceivingTab) {
        if(landToReceivingTab) {
            pagerState.scrollToPage(1)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                }
            }
        )
    }) {
        Column(modifier = Modifier.padding(it)) {
            TransferTabs(listOf(TransferTab.Sending, TransferTab.Receiving),
                pagerState.currentPage,
                uiState.sendingSpeed,
                uiState.sendingItemCount,
                uiState.receivingItemSpeed
            ) { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState,
                pageSize = PageSize.Fill) { pageIndex ->
                if(pageIndex == 0) {
                    TransferSendingScreen()
                } else {
                    TransferReceivingScreen()
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
                horizontalArrangement = Arrangement.Center) {
                val total = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${stringResource(R.string.sent)} : ")
                    }
                    append(uiState.totalSentAndReceived.first)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" \t ${stringResource(R.string.received)} : ")
                    }
                    append(uiState.totalSentAndReceived.second)
                }
                Text(text = total,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }

    }
}

@Composable
private fun TransferTabsOld(
    tabList: List<TransferTab>,
    selected: Int,
    sendSpeed : String,
    sendCount : String,
    receiveSpeed: String,
    onTabClick: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        tabList.forEachIndexed { index, it ->
            val countAndSpeedText = if(index == 0) {
                if(sendCount.isEmpty()) "$sendSpeed/s"
                else "($sendCount) $sendSpeed/s"
            } else {
                "$receiveSpeed/s"
            }
            Text(
                text = it.name + "\n$countAndSpeedText",
                style = if(selected == it.ordinal) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodySmall
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onTabClick(it.ordinal)
                    }
            )
        }
    }
}

@Composable
private fun TransferTabs(
    tabList: List<TransferTab>,
    selectedTab: Int,
    sendSpeed : String,
    sendCount : String,
    receiveSpeed: String,
    onTabClick: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        divider = {},
        indicator = {
            Box(modifier = Modifier
                .tabIndicatorOffset(it[selectedTab])
                .fillMaxSize()
                .clip(CircleShape)
                .zIndex(-1f)
                .background(MaterialTheme.colorScheme.primaryContainer))
        })
    {
        tabList.forEachIndexed { index, it ->
            val countAndSpeedText = if(index == 0) {
                if(sendCount.isEmpty()) "$sendSpeed/s"
                else "($sendCount) $sendSpeed/s"
            } else {
                "$receiveSpeed/s"
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                SendArrow(
                    rotation = if(it.ordinal == 0) {
                        -90f
                    } else 90f
                )
                Text(
                    text = it.name + "\n$countAndSpeedText",
                    style = if(selectedTab == it.ordinal) {
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable {
                            onTabClick(it.ordinal)
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    TransferTabs(
        tabList = listOf(TransferTab.Sending, TransferTab.Receiving),
        selectedTab = 1,
        sendCount = "12 MB/s",
        sendSpeed = "2",
        receiveSpeed = "11 MB/s"
    ) { }
}