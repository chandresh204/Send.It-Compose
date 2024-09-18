package chad.orionsoft.sendit.ui.qr_connect

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.QRDomain
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrConnectScreen(vModel : QrConnectViewModel = hiltViewModel()) {
    val uiState by vModel.qrState.collectAsState()
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
        MainScreen(modifier = Modifier.padding(innerPadding),
            uiState.qrBitmap, uiState.isLoading)
    }
}

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap?,
    isLoading: Boolean
) {
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            println("scan result >>>> ${result.contents}")
        }
    )
    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Box {
            if(qrBitmap == null) {
                Image(
                    painterResource(id = R.drawable.sendit_icon_play),
                    modifier = Modifier
                        .size(250.dp)
                        .alpha(0.5f),
                    contentDescription = null)
            } else {
                Image(bitmap = qrBitmap.asImageBitmap(),
                    modifier = Modifier.size(250.dp),
                    contentDescription = null)
           }
            if(isLoading) CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Connect to this device by Scanning the QR Code",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.padding(16.dp))
        if(qrBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                HorizontalDivider(
                    thickness = 4.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Button(onClick = {
            scanLauncher.launch(ScanOptions())
        }) {
            Text(text = "Scan QR")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    MainScreen(
        qrBitmap = QRDomain.getQRBitmap("dummy Qr code", 512),
        isLoading = true
    )
}