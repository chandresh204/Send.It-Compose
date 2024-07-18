package chad.orionsoft.sendit.ui.sender_image

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SenderImagesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(SenderImagesState())
    val uiState = _uiState.asStateFlow()

    private val tag = "SenderImageViewModel"

    init {
        _uiState.update {
            it.copy(
                images = getAllImages().toMutableList()
            )
        }
    }



    fun onEvent(event: SenderImagesEvent) {
        when(event) {
            is SenderImagesEvent.OnSelectClick -> {
                changeSelectionStateOfImage(event.id)
            }

            /*SenderImagesEvent.OnClearAllSelection -> {
                clearAllSelection()
            }
            SenderImagesEvent.OnSelectAll -> {
                selectAll()
            }*/
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
                /*    selectedSize = if(addOrRemove) {
                        onItemSelectionChanged(oldList[index].imageData, true)
                        it.selectedSize + oldList[index].imageData.size
                    } else {
                        onItemSelectionChanged(oldList[index].imageData, false)
                        it.selectedSize - oldList[index].imageData.size
                    },*/
                )
            }
            /*_uiState.update {
                it.copy(
                    selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
                )
            }*/
        }
    }

    private fun getAllImages() : List<ImageUiData> {
        return mediaRepository.getImages().map {
            ImageUiData(it, false, null)
        }
    }

   /* private fun clearAllSelection() {
        val oldList = uiState.value.images.toMutableList()
        for(i in oldList.indices) {
            oldList[i] = oldList[i].copy(
                isSelected = false
            )
        }
        _uiState.update {
            it.copy(
                images = oldList,
                selectedCount = 0,
                selectedSize = 0L
            )
        }
        updateTotalSelectedSize()
    }

    private fun selectAll() {
        val oldList = uiState.value.images.toMutableList()
        var selectedCount = 0
        var totalSize = 0L
        for (i in oldList.indices) {
            oldList[i] = oldList[i].copy(
                isSelected = true
            )
            selectedCount++
            totalSize += oldList[i].imageData.size
        }
        _uiState.update {
            it.copy(
                images = oldList,
                selectedCount = selectedCount,
                selectedSize = totalSize
            )
        }
        updateTotalSelectedSize()
    }

    private fun updateTotalSelectedSize() {
        _uiState.update {
            it.copy(
                selectedSizeToDisplay = Converters.formatDataString(it.selectedSize)
            )
        }
    } */

}