package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import chad.orionsoft.sendit.model.ConnectedDeviceDetails
import chad.orionsoft.sendit.model.UDPResponse
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.create_connection_flow.state.ReceiverData
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.RECEIVER_SELECT_MESSAGE
import chad.orionsoft.sendit.utils.Converters
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.sendData
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject


class SenderConnectionProcess @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    @ApplicationContext private val context: Context
) {

    private var operationalPort = Constants.DEFAULT_BROADCAST_PORT
    private val tag = "SenderConnectionProcess"
    private val senderScope = CoroutineScope(Dispatchers.IO)
    private var opSocket : DatagramSocket? = null
    private var selectedReceiver: ReceiverData? = null

    private val _enterCodeTimeChannel = Channel<Int>()
    val enterCodeTimerReceiver = _enterCodeTimeChannel.receiveAsFlow()

    suspend fun initProcess() : Flow<UDPResponse> {
        val channel = Channel<UDPResponse>()
        if(stillRunning) {
            Log.e(tag, "init failed, process already running")
        }
        senderScope.launch {
            val broadcastAddress = obtainBroadcastAddress()
            if(broadcastAddress == null) {
                Log.e(tag, "Failed to obtain broadcast address")
                channel.send(
                    UDPResponse(
                        success = false,
                        response = "Failed to obtain broadcast address",
                        partnerPort = -1,
                        partnerAddress = InetAddress.getLocalHost()
                    )
                )
                return@launch
            }
            opSocket = DatagramSocket()
            opSocket?.soTimeout = 4000
            while (isActive && selectedReceiver == null) {
                stillRunning = true
                Log.d(tag, "s2 >>> sending broadcast")
                val result = opSocket?.sendData(
                    Constants.BROADCAST_MSG, InetAddress.getByName(broadcastAddress),
                    operationalPort
                )
                if(result?.success == true && result.response.contains(Constants.BROADCAST_MSG)) {
                    channel.send(result)
                    Log.d(tag, "s3 >>> collect receiver")
                }
                delay(3000)
            }
            if(selectedReceiver != null) {
                Log.d(tag, "s4 >>> sending select signal")
                val selectRes = opSocket?.sendData(
                    "$RECEIVER_SELECT_MESSAGE((${Constants.DEVICE_TYPE_PHONE}))${RuntimeConstants.myColor}/*${createMyUsername()}*/",
                    selectedReceiver!!.address, operationalPort)
                if(selectRes?.success == true && selectRes.response.contains(RECEIVER_SELECT_MESSAGE)) {
                    val infoStr = selectRes.response
                    Log.d(tag, "s5 >>> received device info : $infoStr")
                    try {
                        val deviceId = Converters.extractDataInNormalCase(infoStr)
                        val newPortStartIndex = infoStr.indexOf("*/")
                        val newPortEndIndex = infoStr.indexOf("#")
                        val appVersionEndIndex = infoStr.indexOf("!%")
                        val newPort = infoStr.substring(newPortStartIndex+2, newPortEndIndex).toInt()
                        val appVersion = infoStr.substring(newPortEndIndex+1, appVersionEndIndex).toInt()
                        val freeSpace = infoStr.substring(appVersionEndIndex+2).toLong()
                        if(appVersion < Constants.MIN_SUPPORT_VERSION) {
                            Log.d(tag, "s5_1 >>> send not supported signal")
                            opSocket?.sendData(
                                Constants.RECEIVER_NOT_SUPPORTED + "/*OldVersion*/",
                                selectedReceiver!!.address, operationalPort,
                                waitForResponse = false
                            )
                            channel.send(
                                selectRes.copy(
                                    success = false,
                                    response = "Receiver using old version, Can not continue!!"
                                )
                            )
                            return@launch
                        } else {
                            Log.d(tag, "s5_1 >>> sending okay signal")
                            opSocket?.sendData(
                                Constants.CONNECT_NOW + "/*ok*/",
                                selectedReceiver!!.address, operationalPort,
                                waitForResponse = false
                            )
                        }
                        Log.d(tag, "Moving to new port")
                        operationalPort = newPort
                        launch {
                            moveProcessToNewPort(deviceId, freeSpace, channel)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "s5 >>> error extracting device info: $e")
                        channel.send(
                            selectRes.copy(
                                success = false,
                                response = ""
                            )
                        )
                    }

                }
            }
        }
        return channel.receiveAsFlow()
    }

    private suspend fun moveProcessToNewPort(deviceId: String, freeSpace: Long,
                                             updateChannel: Channel<UDPResponse> ) = coroutineScope {
        Log.d(tag, "s6 >>> check if receiver is known or not")
        val isKnown = preferenceRepository.getKnownReceivers()?.contains(deviceId) ?: false
        delay(3000)
        if(isKnown) {
            opSocket?.sendData(
                Constants.CONNECT_NOW,
                selectedReceiver?.address!!,
                operationalPort,
                waitForResponse = false
            )
            Log.d(tag, "s7_a1 >>> send connected ok signal")
            updateChannel.send(
                UDPResponse(
                    success = true,
                    partnerAddress = InetAddress.getLocalHost(),
                    partnerPort = -1,
                    response = Constants.CONNECT_NOW
                )
            )
            setConnectedDevice(selectedReceiver, freeSpace)
        } else {
            val connectCode = RuntimeConstants.generateRandomCode(4)
            updateChannel.send(
                UDPResponse(
                    success = true,
                    partnerAddress = InetAddress.getLocalHost(),
                    partnerPort = -1,
                    response = "${Constants.ENTER_CODE_SIGNAL}/*$connectCode*/"
                )
            )
            Log.d(tag,"s7_b1 >>> send enter code request")
            var countDown = Constants.ENTER_CODE_TIME_OUT
            launch {
                do {
                    _enterCodeTimeChannel.send(countDown)
                    delay(1000)
                    countDown--
                } while (countDown > 0)
            }
            val codeResponse = opSocket?.sendData(
                Constants.ENTER_CODE_SIGNAL,
                selectedReceiver?.address!!,
                operationalPort,
                soTimeout1 = Constants.ENTER_CODE_TIME_OUT * 1000)
            countDown = 0
            if(codeResponse?.success == true) {
                val recCode = Converters.extractDataInNormalCase(codeResponse.response)
                Log.d(tag, "s7_b2 -> received code: $recCode")
                Log.d(tag, "s7_b3 -> check received code and reply whatever the result")
                val codeResult = if(recCode == connectCode) {
                    updateChannel.send(
                        UDPResponse(
                            success = true,
                            partnerAddress = InetAddress.getLocalHost(),
                            partnerPort = -1,
                            response = Constants.CONNECT_NOW
                        )
                    )
                    setConnectedDevice(selectedReceiver, freeSpace)
                    preferenceRepository.addToKnownReceiver(deviceId)
                    "true"
                } else {
                    updateChannel.send(
                        UDPResponse(
                            success = false,
                            partnerAddress = InetAddress.getLocalHost(),
                            partnerPort = -1,
                            response = "Receiver sent wrong code"
                        )
                    )
                    "false"
                }
                opSocket?.sendData(
                    Constants.CODE_ENTERED_SIGNAL_RESULT + "/*$codeResult*/",
                    selectedReceiver?.address!!,
                    operationalPort,
                    waitForResponse = false
                )
            } else {
                updateChannel.send(
                    UDPResponse(
                        success = false,
                        partnerAddress = InetAddress.getLocalHost(),
                        partnerPort = -1,
                        response = "Time Out"
                    )
                )
            }
        }
        opSocket?.close()
        stillRunning = false
    }

    fun selectReceiver(receiverData: ReceiverData) {
        selectedReceiver = receiverData
    }

    private suspend fun obtainBroadcastAddress(): String? {
        if (RuntimeConstants.BROAD_ADDR != null) {
            Log.d(tag, "s1 >>> Obtained broadcast address : ${RuntimeConstants.BROAD_ADDR}")
            println("broadcast address: ${RuntimeConstants.BROAD_ADDR}")
            return RuntimeConstants.BROAD_ADDR
        }
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val mobDataState = tm.dataState
        return withContext(Dispatchers.IO) {
            val dSocket = DatagramSocket()
            while (RuntimeConstants.BROAD_ADDR == null) {
                for (i in 0 until 256) {
                    val host = "192.168.$i.255"
                    if (mobDataState == 0) {
                        dSocket.soTimeout = 15000
                    } else {
                        dSocket.soTimeout = 100
                    }
                    val response = dSocket.sendData(
                        Constants.BROADCAST_MSG,
                        InetAddress.getByName(host),
                        waitForResponse = false,
                        toPort = operationalPort
                    )
                    if (response.success) {
                        println("broadcast address: $host")
                        RuntimeConstants.BROAD_ADDR = host
                        Log.d("CreateConnection", "s1 -> Obtained broadcast address: ${RuntimeConstants.BROAD_ADDR}")
                        dSocket.close()
                        return@withContext host
                    }
                }
            }
            return@withContext RuntimeConstants.BROAD_ADDR
        }
    }

    private fun createMyUsername(): String {
        val uName = preferenceRepository.getUserNameFromPreferences()
        return "$uName - ${Build.MANUFACTURER}:${Build.MODEL}"
    }

    private fun setConnectedDevice(receiverData: ReceiverData?,
                                   freeSpace: Long) {
        if(receiverData == null) {
            Log.e(tag, "can not set connected device, receiver data is null")
            return
        }
        RuntimeConstants.connectedDeviceDetails =
            ConnectedDeviceDetails(
                receiverData.name,
                receiverData.address,
                receiverData.deviceType,
                receiverData.color,
                freeSpace
            )
    }

    companion object {
        var stillRunning = false
    }
}