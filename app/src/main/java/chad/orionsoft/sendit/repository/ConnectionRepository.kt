package chad.orionsoft.sendit.repository

import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor() {

    private val repoScope = CoroutineScope(Dispatchers.IO)

    private val _connectionUpdateChannel = Channel<ConnectedDeviceDetails?>()
    val connectionUpdateReceiver = _connectionUpdateChannel.receiveAsFlow()

    private var connectedDeviceDetails : ConnectedDeviceDetails? = null

    fun updateConnectionInfo(deviceInfo: ConnectedDeviceDetails?) {
        connectedDeviceDetails = deviceInfo
        repoScope.launch {
            _connectionUpdateChannel.send(deviceInfo)
        }
    }

    fun getConnectedDevice() : ConnectedDeviceDetails? = connectedDeviceDetails
}