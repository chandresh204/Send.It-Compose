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
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import javax.inject.Inject

class NetworkObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // TODO remove this
    private val isWLANPossible = Channel<Boolean>()
    val wlanChangeReceiver = isWLANPossible.receiveAsFlow()

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
//            getBroadcastAddressOfWifi()
            val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
            if(linkProperties != null) {
                linkAddress = linkProperties.linkAddresses.find {
                    it.toString().contains("192.168")
                }.toString()
                Log.d("Link Properties", "Link Address: $linkAddress")
                }
            ioScope.launch {
                isWLANPossible.send(true)
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
            isWLANPossible.send(isOnHotSpot)
        }
        ioScope.launch {
            if(isOnHotSpot) {
                _networkStatus.send(ConnectStatus.OnHotspot)
            } else {
                _networkStatus.send(ConnectStatus.NoNetwork)
            }
        }
    }

    fun getWifiAddress() = linkAddress

    /*fun getWifiAddress() : String? {
        val wm = context.getSystemService(WIFI_SERVICE) as WifiManager
        var connInfo = wm.connectionInfo.ipAddress
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            connInfo = Integer.reverseBytes(connInfo)
        }
        val ipByteArray = BigInteger.valueOf(connInfo.toLong()).toByteArray()
        return try {
            InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (e: UnknownHostException) {
            Log.e("NetworkObserver", "getting broadcast address on wifi: $e")
            null
        }
    }*/

    private fun getBroadcastAddressOfWifi() {
        val wm = context.getSystemService(WIFI_SERVICE) as WifiManager
        var connInfo = wm.connectionInfo.ipAddress
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            connInfo = Integer.reverseBytes(connInfo)
        }
        val ipByteArray = BigInteger.valueOf(connInfo.toLong()).toByteArray()
        try {
            val ipString = InetAddress.getByAddress(ipByteArray).hostAddress
            val lastIndexOfDot = ipString?.lastIndexOf('.') ?: 0
            val broadAddress = ipString?.substring(0, lastIndexOfDot) + ".255"
            RuntimeConstants.BROAD_ADDR = broadAddress
        } catch (e: UnknownHostException) {
            Log.e("NetworkObserver", "getting broadcast address on wifi: $e")
        }
    }

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