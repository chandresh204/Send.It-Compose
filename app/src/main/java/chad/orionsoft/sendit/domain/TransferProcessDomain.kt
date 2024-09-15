package chad.orionsoft.sendit.domain

import android.net.Uri
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.model.ProgressStatus
import chad.orionsoft.sendit.model.ReceivingItemProgressData
import chad.orionsoft.sendit.model.SendingItemProgressData
import chad.orionsoft.sendit.model.TransferringMediaItem
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

object TransferProcessDomain {

    private val transferSendingList = mutableListOf<TransferringMediaItem>()
    private val sentHistoryList = mutableListOf<TransferringMediaItem>()

    private val transferReceivingList = mutableListOf<TransferringMediaItem>()

    private val _transferReceivingListFlow = Channel<TransferringMediaItem>()
    val transferReceivingListFlow = _transferReceivingListFlow.receiveAsFlow()

    // sending flows
    private val _sendingItemProgressFlow = Channel<SendingItemProgressData>()
    val sendingItemProgressFlow = _sendingItemProgressFlow.receiveAsFlow()

    private val _sendingItemStatusFlow = Channel<Pair<Long, ProgressStatus>>()
    val sendingItemStatusFlow = _sendingItemStatusFlow.receiveAsFlow()

    private val _sendingItemCountFlow = MutableSharedFlow<Pair<Int,Int>>()

    /**
     * Sends the flow of items count update, first element represents completed count (whether
     * success or failed), second element represents total number of items (in queue + finished).
     * if first element is [-1] then there is no update in completed count
     */
    val sendingItemCountFlow = _sendingItemCountFlow.asSharedFlow()

    private val _transferSpeedFlow = MutableSharedFlow<Pair<Long, Long>>()


    /**
     * Sends the flow of transfer rate in Pair, first element represents sent Bytes in 1 sec while
     * second element represents received Bytes in 1 sec
     */
    val transferSpeedFlow = _transferSpeedFlow.asSharedFlow()

    // receiving flow
    private val _receivingItemProgressFlow = Channel<ReceivingItemProgressData>()
    val receivingItemProgressData = _receivingItemProgressFlow.receiveAsFlow()

    private val _receivingItemStatusFlow = Channel<Pair<Long, ProgressStatus>>()
    val receivingItemStatusFlow = _receivingItemStatusFlow.receiveAsFlow()

    private val _receivingItemThumbnailFlow = Channel<Pair<Long, Uri>>()
    val receivingItemThumbnailFlow = _receivingItemThumbnailFlow.receiveAsFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var totalSent = 0L
    private var totalReceived = 0L
    var totalSentCount = 0

    /**
     * Shows the current status of the sending process, true means sending is currently in progress
     */
    var isSending = false

    init {
        startCalculatingTransferSpeed()
    }

    fun addToSendingList(mediaList: List<MediaData>) {
        transferSendingList.addAll(mapToTransferringMediaItems(mediaList))
    }

    fun addToReceivingList(filename: String, size: Long) : Long { // returns the id of new file
        val newId = Random.nextLong(1000000, 9999999)
        val newReceivingItem = TransferringMediaItem(
            newId,
            filename,
            null,
            null,
            Converters.getPlaceHolderIconsAsPerFileName(filename),
            size,
            Converters.getFileTypeFromFileName(filename),
            ProgressStatus.Receiving
        )
        transferReceivingList.add(newReceivingItem)
        ioScope.launch {
            _transferReceivingListFlow.send(newReceivingItem)
        }
        return newId
    }

    private fun mapToTransferringMediaItems(mediaList: List<MediaData>) : List<TransferringMediaItem> {
        return mediaList.map {
            TransferringMediaItem(
               id = it.id,
                title = it.title,
                uri = it.uri,
                thumbnailUri = it.thumbnail,
                thumbnailPlaceHolder = Converters.getPlaceHolderIconsAsPerMediaType(it.mediaType),
                size = it.size,
                it.mediaType,
                status = ProgressStatus.InQueue
            )
        }
    }

    private fun startCalculatingTransferSpeed() {
        ioScope.launch {
            while (true) {
                val lastSent = totalSent
                val lastReceived = totalReceived
                delay(1000)
                val sentSpeed = totalSent - lastSent
                val receivedSpeed = totalReceived - lastReceived
                val speedPair = Pair(
                    if(sentSpeed < 0) 0 else sentSpeed,
                    if(receivedSpeed < 0) 0 else receivedSpeed
                )
                _transferSpeedFlow.emit(speedPair)
            }
        }
    }

    fun updateItemSentProgress(id: Long, sent: Long) {
        ioScope.launch {
            _sendingItemProgressFlow.send(SendingItemProgressData(id, sent))
        }
    }

    fun addToTotalSentInSession(sent: Int) {
        totalSent += sent
    }

    fun getTotalSentInSession() : Long = totalSent

    fun addToTotalReceivedInSession(received: Int) {
        totalReceived += received
    }

    fun getTotalReceivedInSession() : Long = totalReceived

    fun updateItemReceivedProgress(id: Long, received: Long) {
        ioScope.launch {
            _receivingItemProgressFlow.send(ReceivingItemProgressData(id, received))
        }
    }

    fun updateItemTransferStatus(id: Long, status: ProgressStatus, isSent: Boolean) { // isSent false means received
        val listToOperate = if(isSent) transferSendingList else transferReceivingList

        val iToUpdate = listToOperate.find { it.id == id }
        iToUpdate?.let {
            val index = listToOperate.indexOf(it)
            listToOperate[index] = listToOperate[index].copy(
                status = status
            )
        }
        ioScope.launch {
            if(isSent) {
                _sendingItemStatusFlow.send(Pair(id, status))
            } else {
                _receivingItemStatusFlow.send(Pair(id, status))
            }
        }
    }

    fun itsTimeToUpdateThumbnailUri(thumbUri: Uri, id: Long) {
        val itemInList = transferReceivingList.find { it.id == id }
        itemInList?.let {
            val index = transferReceivingList.indexOf(it)
            transferReceivingList[index] = transferReceivingList[index].copy(
                thumbnailUri = thumbUri
            )
            ioScope.launch {
                _receivingItemThumbnailFlow.send(Pair(id, thumbUri))
            }
        }
    }


    fun updateSendingItemCount(completed: Int) {
        ioScope.launch {
            _sendingItemCountFlow.emit(Pair(completed, transferSendingList.size + sentHistoryList.size))
        }
    }


    fun clearQueueAndMoveToFinish() {
        sentHistoryList.addAll(transferSendingList)
        transferSendingList.clear()
    }

    fun getTheCurrentTransferSendingList() : List<TransferringMediaItem> = transferSendingList
    fun getTheCurrentTransferReceivingList() : List<TransferringMediaItem> = transferReceivingList

    fun getSentHistoryList() = sentHistoryList

    fun removeItemFromQueue(id: Long): Boolean {
        val foundItem = transferSendingList.find { it.id == id }
        return if(foundItem == null) false
        else {
            if (foundItem.status == ProgressStatus.InQueue) {
                transferSendingList.remove(foundItem)
                ioScope.launch {
                    _sendingItemCountFlow.emit(
                        Pair(
                            totalSentCount + 1,
                            transferSendingList.size + sentHistoryList.size
                        )
                    )
                }
                true
            } else false
        }
    }
}