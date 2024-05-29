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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val isWLANPossible = Channel<Boolean>()
    val wlanChangeReceiver = isWLANPossible.receiveAsFlow()

    private val tag = "NetworkObserver"
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var isWifiConnected = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            ioScope.launch {
                isWLANPossible.send(true)
            }
            isWifiConnected = true
            Log.d(tag, "Connected")
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            isWifiConnected = false
            isOnWLAN()
            Log.d(tag, "Lost")
        }
    }

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private fun isOnWLAN() {
        val wManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        val apMethod = wManager.javaClass.getMethod("getWifiApState")
        val apState = apMethod.invoke(wManager) as Int
        val isOnWLAN = if(apState == 13) {
            Log.d(tag, "hotSpot on")
            true
        } else false
        ioScope.launch {
            isWLANPossible.send(isOnWLAN)
        }
    }

    init {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        val apStateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                isOnWLAN()
            }
        }
        context.registerReceiver(apStateChangeReceiver, IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"))
    }
}