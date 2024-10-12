package chad.orionsoft.sendit.ui.sender_image

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.ThumbPaginator
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SenderImagesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val thumbnailDomain: ThumbnailDomain
): ViewModel() {

    private val _uiState = MutableStateFlow(SenderImagesState())
    val uiState = _uiState.asStateFlow()

    private val tag = "SenderImageViewModel"

    private var paginator : ThumbPaginator

    init {
        val allImages = getAllImages().toMutableList()
        _uiState.update {
            it.copy(
                images = allImages
            )
        }
        viewModelScope.launch {
            MainScreenViewModel.clearAllSelectionReceiver.collect {
                if(it) {
                    clearAllSelection()
                }
            }
        }
        paginator = ThumbPaginator(thumbnailDomain, allImages)
        /*viewModelScope.launch {
            paginator.loadNextBundle(0) { newList ->
                _uiState.update {
                    it.copy(images = newList)
                }
            }
        }*/
    }

    private fun startThumbGeneration(images: List<MediaUiData>) {
        for (i in 0 .. 20) {
            val image = try { images[i] } catch (e:Exception) {
                break
            }
            val bMap = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                thumbnailDomain.generateThumbnail(
                    Converters.extractLastElementOfUri(image.mediaData.uri),
                    image.mediaData.pathData ?: "", FileType.FileTypeImage
                )
            } else {
                thumbnailDomain.generateThumbnail(image.mediaData.uri)
            }
            val updateList = uiState.value.images.toMutableList()
            val iToUpdate = updateList.find { image.mediaData.id == it.mediaData.id }
            iToUpdate?.let {
                val index = updateList.indexOf(it)
                val newmediaData = it.copy(
                    thumbnailBitmap = bMap
                )
                updateList[index] = newmediaData
                _uiState.update { state ->
                    state.copy(
                        images = updateList,
                    )
                }
            }
        }
    }



    fun onEvent(event: SenderImagesEvent) {
        when(event) {
            is SenderImagesEvent.OnSelectClick -> {
                changeSelectionStateOfImage(event.id)
            }

            is SenderImagesEvent.OnLastVisibleItemIndexChanged -> {
                println("last visible : ${event.index}")
                viewModelScope.launch {
                    paginator.loadNextBundle(event.index) { newList ->
                        _uiState.update {
                            it.copy(images = newList)
                        }
                    }
                }
            }
        }
    }

    private fun changeSelectionStateOfImage(id: Long) {
        val oldList = uiState.value.images
        val index = oldList.indexOf(oldList.find { it.mediaData.id == id })
        if(index >= 0) {
            oldList[index].selected = !oldList[index].selected
            val addOrRemove = oldList[index].selected
            _uiState.update {
                it.copy(
                    images = oldList,
                    selectedCount = if(addOrRemove) {
                        it.selectedCount + 1
                    } else it.selectedCount - 1,
                )
            }
        }
    }

    private fun getAllImages() : List<MediaUiData> {
        return mediaRepository.getImages().map {
            MediaUiData(it, false, null)
        }
    }

    private fun clearAllSelection() {
        val imageList = _uiState.value.images
        imageList.forEach {
            it.selected = false
        }
        _uiState.update {
            it.copy(
                images = imageList
            )
        }
    }

}