package chad.orionsoft.sendit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.bottom_sheets.WLANConnectionRequiredBottomSheet
import chad.orionsoft.sendit.ui.components.AppBarWithMessage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SenderMainScreen() {
    var appBarMessage by remember {
        mutableStateOf("")
    }
    LaunchedEffect(Unit) {
        delay(3000)
        appBarMessage = "Sample message"
    }
    Scaffold(topBar = {
        AppBarWithMessage(stringResource(R.string.app_name), appBarMessage)
    }) {
        Column(modifier = Modifier.padding(it)) {

        }
    }

    ModalBottomSheet(onDismissRequest = { }) {
        WLANConnectionRequiredBottomSheet()
    }
}