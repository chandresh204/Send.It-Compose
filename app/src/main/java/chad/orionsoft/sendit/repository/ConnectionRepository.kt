package chad.orionsoft.sendit.repository

import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object ConnectionRepository  {

    private val repoScope = CoroutineScope(Dispatchers.IO)

    private var connectedDeviceDetails : ConnectedDeviceDetails? = null

    private val _connectionUpdateChannel = Channel<ConnectedDeviceDetails?>()
    val connectionUpdateReceiver = _connectionUpdateChannel.receiveAsFlow()

    fun updateConnectionInfo(deviceInfo: ConnectedDeviceDetails?) {
        connectedDeviceDetails = deviceInfo
        repoScope.launch {
            _connectionUpdateChannel.send(deviceInfo)
        }
    }

//    fun getConnectedDevice() : ConnectedDeviceDetails? = connectedDeviceDetails
}