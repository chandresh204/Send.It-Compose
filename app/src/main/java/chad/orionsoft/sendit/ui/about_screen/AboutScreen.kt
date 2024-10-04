package chad.orionsoft.sendit.ui.about_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.theme.SendItTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack : () -> Unit) {
    // TODO implement about screen
    Scaffold(
        topBar = {
            TopAppBar(title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                })
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Send.It", style = MaterialTheme.typography.headlineLarge)
            Image(
                painterResource(id = R.drawable.sendit_icon_play), contentDescription = "AppIcon",
                modifier = Modifier.size(200.dp),
            )
            Text(text = "App Version : Unknown")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        AboutScreen {}
    }
}