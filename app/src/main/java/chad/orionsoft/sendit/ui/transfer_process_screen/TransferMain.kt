package chad.orionsoft.sendit.ui.transfer_process_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferTab
import chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving.TransferReceivingScreen
import chad.orionsoft.sendit.ui.transfer_process_screen.transfer_sending.TransferSendingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransferMain(onBack: () -> Unit) {
    val vModel = hiltViewModel<TransferMainViewModel>()
    val uiState by vModel.tMainState.collectAsState()
    val pagerState = rememberPagerState {
        2
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
            HorizontalPager(state = pagerState, pageSize = PageSize.Fill) { pageIndex ->
                if(pageIndex == 0) {
                    TransferSendingScreen()
                } else {
                    TransferReceivingScreen()
                }
            }
        }

    }
}

@Composable
private fun TransferTabs(
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
                "($sendCount) $sendSpeed/s"
            } else {
                receiveSpeed
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TransferMain{}
}