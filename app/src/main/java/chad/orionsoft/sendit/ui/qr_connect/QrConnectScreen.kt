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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.domain.StartActivity
import chad.orionsoft.sendit.model.ConnectStatus
import chad.orionsoft.sendit.ui.bottom_sheets.BottomSheetWith1Button
import chad.orionsoft.sendit.ui.bottom_sheets.WLANConnectionRequiredBottomSheet
import chad.orionsoft.sendit.ui.components.TextWithIcon
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrConnectScreen(vModel : QrConnectViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by vModel.qrState.collectAsState()
    val context = LocalContext.current
    var isBackPressedAlready by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = uiState.connectStatus) {
        if(uiState.connectStatus == ConnectStatus.DeviceConnected) {
            delay(1000)
            if(!isBackPressedAlready) {
                onBack()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(R.string.app_name) + " - Connect")
            },
                navigationIcon = {
                    IconButton(onClick = {
                        isBackPressedAlready = true
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
    ) { innerPadding ->
        when(uiState.connectStatus) {
            ConnectStatus.NoNetwork -> {
                vModel.performNetworkGoneOperations()
                WLANConnectionRequiredBottomSheet(
                    onDismiss = onBack,
                    connectToWifiClick = {
                        StartActivity.openWifiSettings(context)
                    }) {
                    StartActivity.openTetherSettings(context)
                }
            }
            ConnectStatus.OnHotspot, ConnectStatus.OnWifi -> {
                NotConnectedScreen(modifier = Modifier.padding(innerPadding),
                    uiState.qrBitmap, qrResult = uiState.qrScanResult) {
                    vModel.processScannedQR(it)
                }
            }
            ConnectStatus.ConnectingDevice -> {
                ConnectionScreen(
                    deviceName = uiState.connectStatus.msg,
                    inProgress = true
                )
            }
            ConnectStatus.DeviceConnected -> {
                ConnectionScreen(
                    deviceName = uiState.pairingDeviceDetails?.name ?: "Unknown",
                    inProgress = false
                )
            }
        }
        uiState.connectionError?.let {
            BottomSheetWith1Button(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                title = it.first,
                subtitle = it.second,
                negativeButton = {
                    TextWithIcon(Icons.Default.Close, text = "Dismiss")
                }) {
                vModel.dismissErrorDialog()
            }
        }
    }
}

@Composable
private fun NotConnectedScreen(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap?,
    qrResult : String?,
    processQrScanned: (String) -> Unit
) {
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            println("scan result >>>> ${result.contents}")
            processQrScanned(result.contents)
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
        qrResult?.let {
            Text(text = "QR Scan Result: $it")
        }
    }
}

@Composable
private fun ConnectionScreen(
    modifier: Modifier = Modifier,
    deviceName: String,
    inProgress : Boolean
) {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)) {
            if(inProgress) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Check,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp),
                    contentDescription = null)
            }

        }
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text =
            if(inProgress) "Connecting ...."
            else "You're now connected with", style = MaterialTheme.typography.titleMedium)
        Text(text = deviceName)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {

}