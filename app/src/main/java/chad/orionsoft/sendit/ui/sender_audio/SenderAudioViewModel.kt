package chad.orionsoft.sendit.ui.sender_audio

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SenderAudioViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val thumbnailDomain: ThumbnailDomain
) : ViewModel() {

    private val _avState = MutableStateFlow(SenderAudioState())
    val avState = _avState.asStateFlow()

    private val tag = "SenderAudioViewModel"

    init {
        val fullList = mediaRepository.generateAudioList().map { data ->
            MediaUiData(data, false, null)
        }
        _avState.update {
            it.copy(
                audioList = fullList
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
                avState.value.audioList.sortedBy { it.mediaData.title }

            }
            SortOrder.ByDate -> {
                avState.value.audioList.sortedByDescending { it.mediaData.lastModified }
            }
        }
        _avState.update {
            it.copy(
                audioList = aList
            )
        }
        generateThumbnails(aList)
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

    fun onEvent(event : SenderAudioEvent) {
        when(event) {
            is SenderAudioEvent.OnSelectClick -> {
                Log.d(tag, "OnClick id: ${event.id}")
                changeSelectionStateOfAudio(event.id)
            }

            is SenderAudioEvent.OnClearAllSelection -> {
                clearAllSelection()
            }
            is SenderAudioEvent.OnSelectAll -> {
                selectAll()
            }
            is SenderAudioEvent.OnSortingChanged -> {
                _avState.update {
                    it.copy(
                        selectedSortOrder = event.newSort
                    )
                }
                sortTheList(event.newSort)
            }
            is SenderAudioEvent.OnSearch -> {
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

    private fun changeSelectionStateOfAudio(id: Long) {
        val oldList = avState.value.audioList.toMutableList()
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
                    audioList = oldList,
                    selectedCount = if(addOrRemove) {
                        it.selectedCount + 1
                    } else {
                        it.selectedCount - 1
                    },
//                    selectedSize = if(addOrRemove) {
//                        it.selectedSize + oldList[index].mediaData.size
//                    } else {
//                        it.selectedSize - oldList[index].mediaData.size
//                    },
                )
            }
//            updateTotalSelectedSize()
        }
    }

    private fun clearAllSelection() {
        val oldList = avState.value.audioList.toMutableList()
        for(i in oldList.indices) {
            oldList[i] = oldList[i].copy(
                selected = false
            )
        }
        _avState.update {
            it.copy(
                audioList = oldList,
                selectedCount = 0
            )
        }
//        updateTotalSelectedSize()
    }

    private fun selectAll() {
        val oldList = avState.value.audioList.toMutableList()
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
                audioList = oldList,
                selectedCount = selectedCount,
            )
        }
//        updateTotalSelectedSize()
    }

    /*private fun updateTotalSelectedSize() {
        _avState.update {
            it.copy(
                selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
            )
        }
    }*/

    private fun createListBySearch(query: String) {
        _avState.update {
            it.copy(
                searchedList = avState.value.audioList.filter { audData ->
                    audData.mediaData.title.lowercase().contains(query) // query already in lower case
                }
            )
        }
    }

    private fun generateThumbnails(audioList : List<MediaUiData>) {
        val thumbScope = CoroutineScope(Dispatchers.IO)
        for (i in 0 .. 50) {
            val audio = try {
                audioList[i]
            } catch (e: Exception) {
                break
            }
            if(audio.thumbnailBitmap != null) continue
            thumbScope.launch {
                val bMap = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    thumbnailDomain.generateThumbnail(
                        Converters.extractLastElementOfUri(audio.mediaData.uri),
                        audio.mediaData.pathData ?: "", FileType.FileTypeAudio)
                } else {
                    thumbnailDomain.generateThumbnail(audio.mediaData.uri)
                }

                val updateList = avState.value.audioList.toMutableList()
                val iToUpdate = updateList.find { audio.mediaData.id == it.mediaData.id }
                iToUpdate?.let {
                    val index = updateList.indexOf(it)
                    val newmediaData = it.copy(
                        thumbnailBitmap = bMap
                    )
                    updateList[index] = newmediaData
                    _avState.update { state ->
                        state.copy(
                            audioList = updateList,
                        )
                    }
                }
            }
        }
    }
}