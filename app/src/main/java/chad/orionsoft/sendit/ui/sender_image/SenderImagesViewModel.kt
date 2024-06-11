package chad.orionsoft.sendit.ui.sender_image

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SenderImagesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _uiState = MutableStateFlow(SenderImagesState())
    val uiState = _uiState.asStateFlow()

    private val tag = "SenderImageViewModel"

    private var maxImageVisitedIndex = 0

    private var isThumbnailLoadingAlreadyRunning = false

    init {
        _uiState.update {
            it.copy(
                images = getAllImages().toMutableList()
            )
        }
//        loadThumbnails()
    }



    fun onEvent(event: SenderImagesEvent) {
        when(event) {
            is SenderImagesEvent.OnSelectClick -> {
                changeSelectionStateOfImage(event.id)
            }

            is SenderImagesEvent.OnGridLastVisibleChanged -> {
                if(event.lastIndex > maxImageVisitedIndex) {
                    if(!isThumbnailLoadingAlreadyRunning) {
                        val startingPoint = maxImageVisitedIndex
                        maxImageVisitedIndex = event.lastIndex
                        loadThumbnails2(startingPoint)
                        Log.d(tag, "Thumbnail loading not running: Starting for $startingPoint to $maxImageVisitedIndex")
                    } else {
                        maxImageVisitedIndex = event.lastIndex
                        Log.d(tag, "Thumbnail already running updating count to $maxImageVisitedIndex")
                    }
                }
            }
        }
    }

    private fun changeSelectionStateOfImage(id: Long) {
        val oldList = uiState.value.images
        val index = oldList.indexOf(oldList.find { it.imageData.id == id })
        if(index >= 0) {
            oldList[index].isSelected = !oldList[index].isSelected
            val addOrRemove = oldList[index].isSelected
            _uiState.update {
                it.copy(
                    images = oldList,
                    selectedCount = if(addOrRemove) {
                        it.selectedCount + 1
                    } else it.selectedCount - 1,
                    selectedSize = if(addOrRemove) {
                        it.selectedSize + oldList[index].imageData.size
                    } else {
                        it.selectedSize - oldList[index].imageData.size
                    },
                )
            }
            _uiState.update {
                it.copy(
                    selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
                )
            }
        }
    }

    private fun getAllImages() : List<ImageUiData> {
        return mediaRepository.getImages().map {
            ImageUiData(it, false, null)
        }
    }

    private fun loadThumbnails2(startingIndex: Int) {
        var i = startingIndex
        do {
            isThumbnailLoadingAlreadyRunning = true
            val imgData = uiState.value.images[i]
            try {
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(
                        imgData.imageData.uri,
                        Size(300, 300), null
                    )
                } else {
                    // TODO compress for performance
                    BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(imgData.imageData.uri)
                    )
                }
                val oldList = uiState.value.images.toMutableList()
                oldList[i] = oldList[i].copy(
                    thumbnailBitmap = thumbnail
                )
                _uiState.update {
                    it.copy(
                        images = oldList
                    )
                }
                Log.e(tag, "finished for $i ")
            } catch (e: Exception) {
                Log.e(tag, "exception in thumbnail: $e")
            }
            i++
        } while (i <= maxImageVisitedIndex )
        isThumbnailLoadingAlreadyRunning = false
    }
}