package chad.orionsoft.sendit.ui.sender_video

import android.content.Context
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
class SenderVideoViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _avState = MutableStateFlow(SenderVideoState())
    val avState = _avState.asStateFlow()

    private val tag = "SenderVideoViewModel"

    private var maxIndexOfLoadThumbnail = 0

    private var isThumbnailGenerationRunning = false

    init {
        _avState.update {
            it.copy(
                videoList = mediaRepository.generateVideoList().map { data ->
                    VideoUiData(data, false, null)
                }
            )
        }
    }

    fun onEvent(event : SenderVideoEvent) {
        when(event) {
            is SenderVideoEvent.OnLastVisibleIndexChanged -> {
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

            is SenderVideoEvent.OnSelectClick -> {
                Log.d(tag, "OnClick id: ${event.id}")
                changeSelectionStateOfVideo(event.id)
            }
        }
    }

    private fun changeSelectionStateOfVideo(id: Long) {
        val oldList = avState.value.videoList.toMutableList()
        val index = oldList.indexOf(oldList.find { it.videoData.id == id })
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
                    selectedSize = if(addOrRemove) {
                        it.selectedSize + oldList[index].videoData.size
                    } else {
                        it.selectedSize - oldList[index].videoData.size
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
            val audData = avState.value.videoList[i]
            try {
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(
                        audData.videoData.uri,
                        Size(300, 300), null
                    )
                } else {
                    // TODO implement
                    null
                }
                val oldList = avState.value.videoList.toMutableList()
                oldList[i] = oldList[i].copy(
                    thumbnailBitmap = thumbnail
                )
                _avState.update {
                    it.copy(
                        videoList = oldList
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