package chad.orionsoft.sendit.ui.sender_audio

import android.util.Log
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SenderAudioViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    private val _avState = MutableStateFlow(SenderAudioState())
    val avState = _avState.asStateFlow()

    private val tag = "SenderAudioViewModel"

    init {
        _avState.update {
            it.copy(
                audioList = mediaRepository.generateAudioList().map { data ->
                    AudioUIData(data, false)
                }
            )
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
            is SenderAudioEvent.OnSortingChangeClicked -> TODO()
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
        val index = oldList.indexOf(oldList.find { it.audioData.id == id })
        val searchList = mutableListOf<AudioUIData>()
        if(avState.value.isSearching) {
            searchList.addAll(avState.value.searchedList)
            val sIndex = searchList.indexOf(searchList.find { it.audioData.id == id })
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
                    } else it.selectedCount - 1,
                    selectedSize = if(addOrRemove) {
                        it.selectedSize + oldList[index].audioData.size
                    } else {
                        it.selectedSize - oldList[index].audioData.size
                    },
                )
            }
            updateTotalSelectedSize()
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
                selectedCount = 0,
                selectedSize = 0L
            )
        }
        updateTotalSelectedSize()
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
            totalSize += oldList[i].audioData.size
        }
        _avState.update {
            it.copy(
                audioList = oldList,
                selectedCount = selectedCount,
                selectedSize = totalSize
            )
        }
        updateTotalSelectedSize()
    }

    private fun updateTotalSelectedSize() {
        _avState.update {
            it.copy(
                selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
            )
        }
    }

    private fun createListBySearch(query: String) {
        _avState.update {
            it.copy(
                searchedList = avState.value.audioList.filter { audData ->
                    audData.audioData.title.lowercase().contains(query) // query already in lower case
                }
            )
        }
    }
}