package chad.orionsoft.sendit.ui.sender_video

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.ThumbPaginator
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.ui.model.SortOrder
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private var paginator : ThumbPaginator? = null

    init {
        val fullList = mediaRepository.generateVideoList().map { data ->
            MediaUiData(data, false, null)
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
                avState.value.videoList.sortedBy { it.mediaData.title }

            }
            SortOrder.ByDate -> {
                avState.value.videoList.sortedByDescending { it.mediaData.lastModified }
            }
        }
        _avState.update {
            it.copy(
                videoList = aList
            )
        }
        startThumbGeneration(aList)
        paginator = ThumbPaginator(thumbnailDomain, aList)
        if(avState.value.isSearching) {
            val searchList = when(sortOrder) {
                SortOrder.ByName -> {
                    avState.value.searchedList.sortedBy { it.mediaData.title }

                }
                SortOrder.ByDate -> {
                    avState.value.searchedList.sortedByDescending { it.mediaData.lastModified }
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

            is SenderVideoEvent.OnLastVisibleItemChanged -> {
                viewModelScope.launch {
                    paginator?.loadNextBundle(event.lastIndex) { newList ->
                        _avState.update { state ->
                            state.copy(
                                videoList = newList,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun changeSelectionStateOfVideo(id: Long) {
        val oldList = avState.value.videoList.toMutableList()
        val index = oldList.indexOf(oldList.find { it.mediaData.id == id })
        val searchList = mutableListOf<MediaUiData>()
        if(avState.value.isSearching) {
            searchList.addAll(avState.value.searchedList)
            val sIndex = searchList.indexOf(searchList.find { it.mediaData.id == id })
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
                        it.selectedSize + oldList[index].mediaData.size
                    } else {
                        it.selectedSize - oldList[index].mediaData.size
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
            totalSize += oldList[i].mediaData.size
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

    private fun startThumbGeneration(videos: List<MediaUiData>) {
        /*val thumbScope = CoroutineScope(Dispatchers.IO)
        thumbScope.launch {
            for (i in 0 .. 50) {
                val video = try {
                    videos[i]
                } catch (e: Exception) {
                     break
                }
                if(video.thumbnailBitmap != null) continue
                val bMap = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    thumbnailDomain.generateThumbnail(
                        Converters.extractLastElementOfUri(video.mediaData.uri),
                        video.mediaData.pathData ?: "", FileType.FileTypeVideo)
                } else {
                    thumbnailDomain.generateThumbnail(video.mediaData.uri)
                }

                val updateList = avState.value.videoList.toMutableList()
                val iToUpdate = updateList.find { video.mediaData.id == it.mediaData.id }
                iToUpdate?.let {
                    val index = updateList.indexOf(it)
                    val newmediaData = it.copy(
                        thumbnailBitmap = bMap
                    )
                    updateList[index] = newmediaData
                    _avState.update { state ->
                        state.copy(
                            videoList = updateList,
                        )
                    }
                }
                }
        }*/
    }

    private fun createListBySearch(query: String) {
        _avState.update {
            it.copy(
                searchedList = avState.value.videoList.filter { audData ->
                    audData.mediaData.title.lowercase().contains(query) // query already in lower case
                }
            )
        }
    }
}