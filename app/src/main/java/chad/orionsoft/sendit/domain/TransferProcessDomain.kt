package chad.orionsoft.sendit.domain

import android.content.Context
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

    private val _sendingItemProgressPercFlow = Channel<Float>()
    val sendingItemProgressPercFlow = _sendingItemProgressPercFlow.receiveAsFlow()

    private val _sendingItemStatusFlow = Channel<Pair<Long, ProgressStatus>>()
    val sendingItemStatusFlow = _sendingItemStatusFlow.receiveAsFlow()

    private val _sendingItemCountFlow = MutableSharedFlow<Pair<Int,Int>>()

    /**
     * Sends the flow of items count update, first element represents completed count (whether
     * success or failed), second element represents total number of items (in queue + finished).
     * if first element is [-1] then there is no update in completed count
     */
    val sendingItemCountFlow = _sendingItemCountFlow.asSharedFlow()

    private val _sendingItemTitleFlow = MutableSharedFlow<String>()

    /**
     * Sends the flow of the title, currently in sending state
     */
    val sendingItemTitleFlow = _sendingItemTitleFlow.asSharedFlow()

    private val _transferSpeedFlow = MutableSharedFlow<Pair<Long, Long>>()


    /**
     * Sends the flow of transfer rate in Pair, first element represents sent Bytes in 1 sec while
     * second element represents received Bytes in 1 sec
     */
    val transferSpeedFlow = _transferSpeedFlow.asSharedFlow()

    private val _receivingItemCountFlow = MutableSharedFlow<Int>()
    val receivingItemCountFlow  = _receivingItemCountFlow.asSharedFlow()

    // receiving flow
    private val _receivingItemProgressFlow = Channel<ReceivingItemProgressData>()
    val receivingItemProgressData = _receivingItemProgressFlow.receiveAsFlow()

    private val _receivingItemProgressPercFlow = Channel<Float>()
    val receivingItemProgressPercFlow = _receivingItemProgressPercFlow.receiveAsFlow()

    private val _receivingItemStatusFlow = Channel<Pair<Long, ProgressStatus>>()
    val receivingItemStatusFlow = _receivingItemStatusFlow.receiveAsFlow()

    private val _receivingItemThumbnailFlow = Channel<Pair<Long, Uri>>()
    val receivingItemThumbnailFlow = _receivingItemThumbnailFlow.receiveAsFlow()

    private val _receivingItemTitleFlow = MutableSharedFlow<String>()
    /**
     * Sends the flow of the title, currently in sending state
     */
    val receivingItemTitleFlow = _receivingItemTitleFlow.asSharedFlow()


    // clear everything
    private val _clearEverythingFlow = MutableSharedFlow<Boolean>()
    val clearEverythingFlow = _clearEverythingFlow.asSharedFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var totalSent = 0L
    private var totalReceived = 0L
    private var currentSendingItemIdAndSize = Pair(-1L, 0L) // Pair(id, totalSize)
    private var currentReceivingItemIdAndSize = Pair(-1L, 0L) // Pair(id,totalSize)
    var totalSentCount = 0
    var totalReceivedCount = 0

    /**
     * Shows the current status of the sending process, true means sending is currently in progress
     */
    var isSending = false

    init {
        startCalculatingTransferSpeed()
    }

    /**
     * adds new selected items to the current sending list, this functions return [Int] which shows
     * the count of the items that are not added because they were already in the list
     */
    fun addToSendingList(mediaList: List<MediaData>) : Int { // returns number of items failed to add
        val newMediaS = mapToTransferringMediaItems(mediaList)
        // DO not add already exists items
        var alreadyExitsItems = 0
        newMediaS.forEach { newMedia ->
            if(transferSendingList.find { it.id == newMedia.id } == null &&
                sentHistoryList.find { it.id == newMedia.id } == null) {
                transferSendingList.add(newMedia)
            } else {
                alreadyExitsItems++
            }
        }
        return alreadyExitsItems
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
        val proc = if(currentSendingItemIdAndSize.first != id) {
            val iToUpdate = transferSendingList.find { it.id == id }
            iToUpdate?.let {  item ->
                currentSendingItemIdAndSize = Pair(item.id, item.size)
                sent.toFloat()/item.size
            }
        } else {
            sent.toFloat()/ currentSendingItemIdAndSize.second
        }
        ioScope.launch {
            _sendingItemProgressPercFlow.send(proc ?: 1f)
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
        val proc = if(currentReceivingItemIdAndSize.first != id) {
            val iToUpdate = transferReceivingList.find { it.id == id }
            iToUpdate?.let {  item ->
                currentReceivingItemIdAndSize = Pair(item.id, item.size)
                received.toFloat()/item.size
            }
        } else {
            received.toFloat()/ currentReceivingItemIdAndSize.second
        }
        ioScope.launch {
            _receivingItemProgressPercFlow.send(proc ?: 1f)
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
                if(status == ProgressStatus.Success) {
                    totalReceivedCount++
                    launch { _receivingItemCountFlow.emit(totalReceivedCount) }
                }
                _receivingItemStatusFlow.send(Pair(id, status))
            }
        }
    }

    fun itsTimeToUpdateUri(uri: Uri, id: Long) {
        val itemInList = transferReceivingList.find { it.id == id }
        itemInList?.let {
            val index = transferReceivingList.indexOf(it)
            transferReceivingList[index] = transferReceivingList[index].copy(
                uri = uri,
                thumbnailUri = uri
            )
            ioScope.launch {
                _receivingItemThumbnailFlow.send(Pair(id, uri))
            }
        }
    }


    fun updateSendingItemCount(completed: Int) {
        ioScope.launch {
            _sendingItemCountFlow.emit(Pair(completed, transferSendingList.size + sentHistoryList.size))
        }
    }

    fun emitTitleOfCurrentSendingItem(title: String) {
        ioScope.launch {
            _sendingItemTitleFlow.emit(title)
        }
    }

    fun emitTitleOfCurrentReceivingItem(title: String) {
        ioScope.launch {
            _receivingItemTitleFlow.emit(title)
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

    fun deleteReceivedFile(id: Long, context: Context) : Boolean {
        val iToDelete = transferReceivingList.find { it.id == id }
        if(iToDelete == null) return false
        else {
            if(FileIOOperations.deleteFile(iToDelete.uri, context)) {
                updateItemTransferStatus(id, ProgressStatus.Deleted, false)
                return true
            } else return false
        }
    }

    fun clearSession() {
        transferSendingList.clear()
        transferReceivingList.clear()
        val emitScope = CoroutineScope(Dispatchers.IO)
        emitScope.launch {
            _clearEverythingFlow.emit(true)
        }
        emitScope.launch {
            _sendingItemCountFlow.emit(Pair(0,0))
        }
    }
}