package chad.orionsoft.sendit.domain

import android.content.Context
import android.os.Build
import android.util.Log
import chad.orionsoft.sendit.model.UDPResponse
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.Constants.BROADCAST_MSG
import chad.orionsoft.sendit.utils.Constants.CODE_ENTERED_SIGNAL_RESULT
import chad.orionsoft.sendit.utils.Constants.CONNECT_NOW
import chad.orionsoft.sendit.utils.Constants.ENTER_CODE_SIGNAL
import chad.orionsoft.sendit.utils.Constants.RECEIVER_NOT_SUPPORTED
import chad.orionsoft.sendit.utils.Constants.RECEIVER_SELECT_MESSAGE
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.getResponse
import chad.orionsoft.sendit.utils.DatagramSocketExtensions.sendData
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import kotlin.random.Random

class ReceiverConnectionProcess @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    @ApplicationContext private val context: Context
) {

    private var operationalPort = Constants.DEFAULT_BROADCAST_PORT
    private val tag = "CreateConnectionReceiver"
    private var receiverIoScope = CoroutineScope(Dispatchers.IO)
    private var currentJob : Job? = null
    private var opSocket : DatagramSocket? = null
    private var stillBroadcasting = false
    var connectCode: String? = null

    private val _enterCodeTimeChannel = Channel<Int>()
    val enterCodeTimerReceiver = _enterCodeTimeChannel.receiveAsFlow()


    suspend fun cancelProcess(result: Boolean) : Boolean {
        opSocket?.close()
        currentJob?.cancelAndJoin()
        Log.d(tag, "process finished")
        return result
    }

    suspend fun initProcess() : Flow<UDPResponse> {
        val responseChannel = Channel<UDPResponse>()

        currentJob = receiverIoScope.launch {
            stillBroadcasting = true
            val dSocket = DatagramSocket(operationalPort)
            opSocket = dSocket
            Log.d(tag, "r1 >>> receiving broadcast")
            dSocket.soTimeout = 60000
            main@ while (isActive) {
                val res = dSocket.getResponse()
                if(res.success) {
                    if(res.response.contains(BROADCAST_MSG)) {
                        // Check Constant file for data pattern
                        dSocket.sendData("$BROADCAST_MSG((${Constants.DEVICE_TYPE_PHONE}))${RuntimeConstants.myColor}/*${createMyUsername()}*/",
                            res.partnerAddress, res.partnerPort, waitForResponse = false)
                        responseChannel.send(res)
                        Log.d(tag, "r2 >>> reply broadcast reply")
                    }
                    if(res.response.contains(RECEIVER_SELECT_MESSAGE)) {
                        // close the socket and open random socket
                        Log.d(tag, "r2_1 >>> selected signal received")
                        responseChannel.send(res)
                        val newPort = Random.nextInt(1000, 9999)

                        Log.d(tag, "r2_2 >>> sending device info")
                        val myDeviceId = preferenceRepository.getMyUniqueId()
                        val res1 = dSocket.sendData(
                            "$RECEIVER_SELECT_MESSAGE/*$myDeviceId*/$newPort#${Constants.CURRENT_APP_SUPPORT_VERSION}!%${FileIOOperations.getMyFreeSpace(context)}",
                            res.partnerAddress, res.partnerPort
                        )
                        if(res1.success) {
                            if(res1.response.contains(RECEIVER_NOT_SUPPORTED)) {
                                responseChannel.send(
                                    res1.copy(
                                        success = false,
                                        response = "Receiver using older version"
                                    )
                                )
                            }
                        } else {
                            responseChannel.send(
                                    res1.copy(
                                        success = false,
                                        response = "Sender Not Responding"
                                    )
                                )
                        }
                        dSocket.close()
                        launch {
                            startConnectionOnNewSocket(newPort, responseChannel)
                        }
                        break
                    }
                } else {
                    responseChannel.send(res)
                    break
                }
            }
            dSocket.close()
            Log.d(tag, "r2 >>> exited the broadcast process")
            stillBroadcasting = false
        }
        return responseChannel.receiveAsFlow()
    }

    private suspend fun startConnectionOnNewSocket(
        newPort: Int, responseChannel: Channel<UDPResponse>) = coroutineScope {
            while(stillBroadcasting) {
                Log.d(tag, "waiting for broadcast process to complete")
                delay(300)

            }
        Log.d(tag, "r3 >>> process started on new port")
        opSocket?.close()
        opSocket = DatagramSocket(newPort)
        opSocket?.soTimeout = 30000
        main@ while (isActive) {
            val res = opSocket?.getResponse()
            if(res?.success == true) {
                val got = res.response
                if(got.contains(CONNECT_NOW)) {
                    responseChannel.send(res)
                    Log.d("CreateConnection", "r5_a1 -> sent connect ok")
                    opSocket?.close()
                    break
                }
                if(got.contains(ENTER_CODE_SIGNAL)) {
                    Log.d("CreateConnection", "r5_a1_1 -> Need to enter code")
                    responseChannel.send(res)
                    var waitingForCodeSince = 0
                    var succes = false
                    do {
                        if(connectCode != null) {
                            val reply = "$ENTER_CODE_SIGNAL/*$connectCode*/"
                            opSocket?.sendData(reply,
                                res.partnerAddress, res.partnerPort, waitForResponse = false)
                            succes = true
                            break
                        }
                        delay(100)
                        if(waitingForCodeSince % 1000 == 0) {
                            launch {
                                _enterCodeTimeChannel.send(Constants.ENTER_CODE_TIME_OUT - waitingForCodeSince / 1000)
                            }
                        }
                        waitingForCodeSince += 100
                    } while (waitingForCodeSince < Constants.ENTER_CODE_TIME_OUT * 1000)
                    if(!succes) {
                        responseChannel.send(
                            res.copy(
                                success = false,
                                response = "Timed Out"
                            )
                        )
                        Log.e(tag, "r5_a1_2 -> code enter time out")
                        opSocket?.close()
                        break@main
                    }
                }
                if(got.contains(CODE_ENTERED_SIGNAL_RESULT)) {
                    connectCode = null
                    responseChannel.send(res)
                    Log.d(tag, "r5_a2 -> received code result: ${res.response}")
                    opSocket?.close()
                    break@main
                }
            } else {
                responseChannel.send(
                    UDPResponse(
                        false, InetAddress.getLocalHost(), -1, "Timed out"
                    )
                )
                break@main
            }
        }
    }

    private fun createMyUsername(): String {
        val uName = preferenceRepository.getUserNameFromPreferences()
        return "$uName - ${Build.MANUFACTURER}:${Build.MODEL}"
    }

}