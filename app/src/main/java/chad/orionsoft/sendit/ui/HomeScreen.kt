package chad.orionsoft.sendit.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetSearching
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith2Buttons
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.NavigationConstants
import chad.orionsoft.sendit.utils.RuntimeConstants
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(navToScreen: (String) -> Unit) {
    Scaffold {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            var viewOffset by remember {
                mutableStateOf(200.dp)
            }
            val animatedOffset by animateDpAsState(
                targetValue = viewOffset,
                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                label = "Slide Animation"
            )
            LaunchedEffect(Unit) {
                delay(500)
                viewOffset = 0.dp
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        0,
                        ((animatedOffset / 2) - 100.dp)
                            .toPx()
                            .toInt()
                    )
                },
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = R.drawable.sendit_icon_play),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(RuntimeConstants.screenWidthDp / 1.5f))
                Text(text = "V XX",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center) // TODO replace with BuildConfig
            }
            ActionCard(modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = animatedOffset), navToScreen = navToScreen)
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier = Modifier, navToScreen: (String) -> Unit) {
    Card(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            ActionItem(iconRes = R.drawable.send_icon1,
                text = stringResource(id = R.string.send)) {
                navToScreen(NavigationConstants.NAV_SENDER_MAIN)
            }
            ActionItem(iconRes = R.drawable.receive_icon_small,
                text = stringResource(id = R.string.receive)) {
                navToScreen(NavigationConstants.NAV_RECEIVER_MAIN)
            }
            ActionItem(iconRes = R.drawable.edit_icon_small,
                text = stringResource(id = R.string.edit)) {
                navToScreen(NavigationConstants.NAV_EDIT)
            }
        }
    }
}

@Composable
fun ActionItem(iconRes: Int, text: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)) {
        Image(painter = painterResource(id = iconRes),
            modifier = Modifier.size(RuntimeConstants.screenWidthDp/4),
            contentDescription = text)
        Text(text = text)
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=720dp,dpi=440")
@Composable
private fun HomeScreenPreview() {
    SendItTheme {
        HomeScreen {}
    }
}