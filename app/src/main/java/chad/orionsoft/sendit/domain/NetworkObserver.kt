package chad.orionsoft.sendit.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import chad.orionsoft.sendit.model.ConnectStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tag = "NetworkObserver"
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var isWifiConnected = false
    private var linkAddress : String? = null

    private val connectivityManager : ConnectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

    private val _networkStatus = Channel<ConnectStatus>()
    val networkStatus = _networkStatus.receiveAsFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            isWifiConnected = true
            val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
            if(linkProperties != null) {
                linkAddress = linkProperties.linkAddresses.find {
                    it.toString().contains("192.168")
                }.toString()
                Log.d("Link Properties", "Link Address: $linkAddress")
                }
            ioScope.launch {
                _networkStatus.send(ConnectStatus.OnWifi)
            }
            Log.d(tag, "Connected")
        }


        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            isWifiConnected = false
            isOnHotspot()
            Log.d(tag, "Lost")
        }
    }

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private fun isOnHotspot() {
        val wManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        val apMethod = wManager.javaClass.getMethod("getWifiApState")
        val apState = apMethod.invoke(wManager) as Int
        val isOnHotSpot = if(apState == 13) {
            Log.d(tag, "hotSpot on")
            true
        } else false
        ioScope.launch {
            if(isOnHotSpot) {
                _networkStatus.send(ConnectStatus.OnHotspot)
            } else {
                _networkStatus.send(ConnectStatus.NoNetwork)
            }
        }
    }

    fun getWifiAddress() = linkAddress

    init {
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        if(linkProperties != null) {
            linkAddress = linkProperties.linkAddresses.find {
                it.toString().contains("192.168")
            }.toString()
            Log.d("Link Properties", "Link Address: $linkAddress")
        } else {
            Log.d("Link Properties", "not available")
        }
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        val apStateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(!isWifiConnected) isOnHotspot()
            }
        }

        context.registerReceiver(apStateChangeReceiver, IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"))
    }
}