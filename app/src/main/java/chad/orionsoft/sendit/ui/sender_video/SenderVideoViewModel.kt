package chad.orionsoft.sendit.ui.sender_video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import chad.orionsoft.sendit.ui.model.SortOrder
import chad.orionsoft.sendit.ui.sender_image.ImageUiData
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SenderVideoViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val thumbnailDomain: ThumbnailDomain
) : ViewModel() {

    private val _avState = MutableStateFlow(SenderVideoState())
    val avState = _avState.asStateFlow()

    private val tag = "SenderVideoViewModel"

    init {
        val fullList = mediaRepository.generateVideoList().map { data ->
            VideoUiData(data, false, null)
        }
        _avState.update {
            it.copy(
                videoList = fullList
            )
        }
        sortTheList(avState.value.selectedSortOrder)
        viewModelScope.launch {
            MainScreenViewModel.clearAllSelectionReceiver.collect {
                if(it) {
                    clearAllSelection()
                }
            }
        }
    }

    private fun sortTheList(sortOrder: SortOrder) {
        val aList = when(sortOrder) {
            SortOrder.ByName -> {
                avState.value.videoList.sortedBy { it.videoData.title }

            }
            SortOrder.ByDate -> {
                avState.value.videoList.sortedByDescending { it.videoData.lastModified }
            }
        }
        _avState.update {
            it.copy(
                videoList = aList
            )
        }
        startThumbGeneration(aList)
        if(avState.value.isSearching) {
            val searchList = when(sortOrder) {
                SortOrder.ByName -> {
                    avState.value.searchedList.sortedBy { it.videoData.title }

                }
                SortOrder.ByDate -> {
                    avState.value.searchedList.sortedByDescending { it.videoData.lastModified }
                }
            }
            _avState.update {
                it.copy(
                    searchedList = searchList
                )
            }
        }
    }

    fun onEvent(event : SenderVideoEvent) {
        when(event) {
            is SenderVideoEvent.OnSelectClick -> {
                Log.d(tag, "OnClick id: ${event.id}")
                changeSelectionStateOfVideo(event.id)
            }

            SenderVideoEvent.OnClearAllSelection -> {
                clearAllSelection()
            }
            SenderVideoEvent.OnSelectAll -> {
                selectAll()
            }
            is SenderVideoEvent.OnSortingChanged -> {
                _avState.update {
                    it.copy(
                        selectedSortOrder = event.sortOrder
                    )
                }
                sortTheList(event.sortOrder)
            }
            is SenderVideoEvent.OnSearch ->  {
                // TODO improve performance by ignoring quick text changes
                if(event.query == null) {
                    _avState.update {
                        it.copy(
                            isSearching = false
                        )
                    }
                } else {
                    _avState.update {
                        it.copy(
                            isSearching = true
                        )
                    }
                    createListBySearch(event.query.lowercase())
                }
            }
        }
    }

    private fun changeSelectionStateOfVideo(id: Long) {
        val oldList = avState.value.videoList.toMutableList()
        val index = oldList.indexOf(oldList.find { it.videoData.id == id })
        val searchList = mutableListOf<VideoUiData>()
        if(avState.value.isSearching) {
            searchList.addAll(avState.value.searchedList)
            val sIndex = searchList.indexOf(searchList.find { it.videoData.id == id })
            if(sIndex >= 0) {
                searchList[sIndex] = searchList[sIndex].copy(
                    selected = !searchList[sIndex].selected,
                )
            }
            _avState.update {
                it.copy(
                    searchedList = searchList,
                )
            }
        }
        if(index >= 0) {
            oldList[index] = oldList[index].copy(
                selected = !oldList[index].selected
            )
            val addOrRemove = oldList[index].selected
            _avState.update {
                it.copy(
                    videoList = oldList,
                    selectedCount = if(addOrRemove) {
                        it.selectedCount + 1
                    } else it.selectedCount - 1,
                    /*selectedSize = if(addOrRemove) {
                        it.selectedSize + oldList[index].videoData.size
                    } else {
                        it.selectedSize - oldList[index].videoData.size
                    },*/
                )
            }
//            updateTotalSelectedSize()
        }
    }

    private fun clearAllSelection() {
        val oldList = avState.value.videoList.toMutableList()
        for(i in oldList.indices) {
            oldList[i] = oldList[i].copy(
                selected = false
            )
        }
        _avState.update {
            it.copy(
                videoList = oldList,
                selectedCount = 0,
//                selectedSize = 0L
            )
        }
//        updateTotalSelectedSize()
    }

    private fun selectAll() {
        val oldList = avState.value.videoList.toMutableList()
        var selectedCount = 0
        var totalSize = 0L
        for (i in oldList.indices) {
            oldList[i] = oldList[i].copy(
                selected = true
            )
            selectedCount++
            totalSize += oldList[i].videoData.size
        }
        _avState.update {
            it.copy(
                videoList = oldList,
                selectedCount = selectedCount,
//                selectedSize = totalSize
            )
        }
//        updateTotalSelectedSize()
    }

//    private fun updateTotalSelectedSize() {
//        _avState.update {
//            it.copy(
//                selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
//            )
//        }
//    }

    private fun startThumbGeneration(videos: List<VideoUiData>) {
        val thumbScope = CoroutineScope(Dispatchers.IO)
        for (i in 0 .. 50) {
            val video = try {
                videos[i]
            } catch (e: Exception) {
                 break
            }
            if(video.thumbnailBitmap != null) continue
            thumbScope.launch {
                val bMap = thumbnailDomain.generateThumbnail(
                    Converters.extractLastElementOfUri(video.videoData.uri),
                    video.videoData.pathData ?: "", FileType.FileTypeVideo)

                val updateList = avState.value.videoList.toMutableList()
                val iToUpdate = updateList.find { video.videoData.id == it.videoData.id }
                iToUpdate?.let {
                    val index = updateList.indexOf(it)
                    val newVideoData = it.copy(
                        thumbnailBitmap = bMap
                    )
                    updateList[index] = newVideoData
                    _avState.update { state ->
                        state.copy(
                            videoList = updateList,
                        )
                    }
                }
            }
        }
    }

    private fun createListBySearch(query: String) {
        _avState.update {
            it.copy(
                searchedList = avState.value.videoList.filter { audData ->
                    audData.videoData.title.lowercase().contains(query) // query already in lower case
                }
            )
        }
    }
}