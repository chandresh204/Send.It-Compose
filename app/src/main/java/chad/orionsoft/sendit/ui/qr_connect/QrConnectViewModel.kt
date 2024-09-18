package chad.orionsoft.sendit.ui.qr_connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.QRDomain
import chad.orionsoft.sendit.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QrConnectViewModel : ViewModel() {

    private val _qrState = MutableStateFlow(QrConnectState())
    val qrState = _qrState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            val sampleQr = "This is sample QR code"
            val bitmap = QRDomain.getQRBitmap(sampleQr, Constants.QR_CODE_SIZE)
            bitmap?.let { bMap ->
                _qrState.update {
                    it.copy(
                        canShowQR = true,
                        qrBitmap = bMap,
                        isLoading = false
                    )
                }
            }

        }
    }
}