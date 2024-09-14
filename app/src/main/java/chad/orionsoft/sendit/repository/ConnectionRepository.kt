package chad.orionsoft.sendit.repository

import android.util.Log
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.model.MediaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object ConnectionRepository  {

    private val repoScope = CoroutineScope(Dispatchers.IO)
    private val tag = "ConnectionRepository"
    private var connectedDeviceDetails : ConnectedDeviceDetails? = null

    private val _connectionUpdateChannel = MutableSharedFlow<ConnectedDeviceDetails?>()
    val connectionUpdateReceiver = _connectionUpdateChannel.asSharedFlow()


    fun updateConnectionInfo(deviceInfo: ConnectedDeviceDetails?) {
        connectedDeviceDetails = deviceInfo
        repoScope.launch {
            _connectionUpdateChannel.emit(deviceInfo)
        }
    }

    fun getConnectedDevice() : ConnectedDeviceDetails? = connectedDeviceDetails
}