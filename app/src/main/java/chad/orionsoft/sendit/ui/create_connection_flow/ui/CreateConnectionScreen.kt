package chad.orionsoft.sendit.ui.create_connection_flow.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.ui.theme.SendItTheme

@Composable
fun CreateConnectionScreen(onBack : (Boolean) -> Unit) {
    var creatingConnectionMode by remember {
        mutableIntStateOf(0)
    }
    Scaffold {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                creatingConnectionMode = 1
            }) {
                Text(text = "Send Request")
            }
            Button(onClick = {
                creatingConnectionMode = 2
            }) {
                Text(text = "Accept Request")
            }
        }
    }
    when(creatingConnectionMode) {
        1 -> {
            CreateConnectionSender {
                onBack(it)
            }
        }
        2 -> {
            CreateConnectionReceiver {
                onBack(it)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    SendItTheme {
        CreateConnectionScreen {}
    }
}