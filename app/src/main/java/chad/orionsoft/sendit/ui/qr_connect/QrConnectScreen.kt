package chad.orionsoft.sendit.ui.qr_connect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.QRDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrConnectScreen() {
    val msg = "this is qr code message"
    val bitmap = QRDomain.getQRBitmap(msg, 512)
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(R.string.app_name) + " - Connect")
            },
                navigationIcon = {
                    IconButton(onClick = {  }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = "Msg in QR")
            bitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    QrConnectScreen()
}