package chad.orionsoft.sendit.ui.sender_audio

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SenderAudioViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _avState = MutableStateFlow(SenderAudioState())
    val avState = _avState.asStateFlow()

    private val tag = "SenderAudioViewModel"

    private var maxIndexOfLoadThumbnail = 0

    private var isThumbnailGenerationRunning = false

    init {
        _avState.update {
            it.copy(
                audioList = mediaRepository.generateAudioList().map { data ->
                    AudioUIData(data, false, null)
                }
            )
        }
    }

    fun onEvent(event : SenderAudioEvent) {
        when(event) {
            is SenderAudioEvent.OnLastVisibleIndexChanged -> {
                if(event.lastIndex > maxIndexOfLoadThumbnail) {
                    if(!isThumbnailGenerationRunning) {
                        val startingPoint = maxIndexOfLoadThumbnail
                        maxIndexOfLoadThumbnail = event.lastIndex
                        viewModelScope.launch {
                            loadThumbnail(startingPoint)
                        }
                    } else {
                        maxIndexOfLoadThumbnail = event.lastIndex
                    }
                }
            }

            is SenderAudioEvent.OnSelectClick -> {
                Log.d(tag, "OnClick id: ${event.id}")
                changeSelectionStateOfAudio(event.id)
            }
        }

    }

    private fun changeSelectionStateOfAudio(id: Long) {
        val oldList = avState.value.audioList.toMutableList()
        val index = oldList.indexOf(oldList.find { it.audioData.id == id })
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
            _avState.update {
                it.copy(
                    selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
                )
            }
        }
    }

    private fun loadThumbnail(startingIndex: Int) {
        var i = startingIndex
        do {
            isThumbnailGenerationRunning = true
            val audData = avState.value.audioList[i]
            try {
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(
                        audData.audioData.albumArt,
                        Size(100, 100), null
                    )
                } else {
                    // TODO compress for performance
                    Log.d(tag,"Album Art in < Q >>> ${audData.audioData.albumArt}")
                    BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(audData.audioData.albumArt)
                    )
                }
                val oldList = avState.value.audioList.toMutableList()
                oldList[i] = oldList[i].copy(
                    thumbnailBitmap = thumbnail
                )
                _avState.update {
                    it.copy(
                        audioList = oldList
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "exception in thumbnail: $e")
            }
            Log.d(tag, "thumbnail finished for: $i")
            i++
        } while (i < maxIndexOfLoadThumbnail)
        isThumbnailGenerationRunning = false
    }
}