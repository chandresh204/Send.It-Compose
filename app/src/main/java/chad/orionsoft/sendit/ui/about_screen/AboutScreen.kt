package chad.orionsoft.sendit.ui.about_screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack : () -> Unit) {
    // TODO add icons in button and app version
    val scrollState = rememberScrollState()
    val context = LocalContext.current
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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)
            .padding(horizontal = Constants.PAGE_PADDING.dp)
            .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Image(
                painterResource(id = R.drawable.sendit_icon_play), contentDescription = "AppIcon",
                modifier = Modifier.size(200.dp),
            )
            Text(text = stringResource(id = R.string.app_intro))
            Text(
                text = stringResource(id = R.string.when_use_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(id = R.string.when_use_desc))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = {
                    val shareMsg = context.getString(R.string.share_app_message) +
                            "\n${Constants.PLAYSTORE_URL}"
                    val i = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, shareMsg)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(i, "Share via"))
                }) {
                    TextWithIcon(imageVector = Icons.Default.Share, text = stringResource(id = R.string.share_this_app))
                }
                OutlinedButton(onClick = {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(Constants.PLAYSTORE_URL)
                    context.startActivity(i)
                }) {
                    TextWithIcon(imageVector = Icons.Default.Star, text = stringResource(id = R.string.rate_app))
                }
            }
            Text(text = "App Version : ${Constants.APP_VERSION}")
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