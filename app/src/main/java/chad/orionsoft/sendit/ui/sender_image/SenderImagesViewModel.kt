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
    private val mediaRepository: MediaRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SenderImagesState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                images = getAllImages()
            )
        }
    }

    fun onEvent(event: SenderImagesEvent) {
        when(event) {
            is SenderImagesEvent.OnSelectClick -> {
                changeSelectionStateOfImage(event.id)
            }
        }
    }

    private fun changeSelectionStateOfImage(id: Long) {
        val updatedList = mutableListOf<ImageUiData>()
        updatedList.addAll(uiState.value.images)
//        val img = updatedList.find { it.imageData.== id }
//        img?.let {  img1 ->
//            img1.isSelected = !img1.isSelected
//        }
//        _uiState.update {
//            it.copy(
//                images = updatedList
//            )
//        }
    }

    private fun getAllImages() : List<ImageUiData> {
        return mediaRepository.getImagesQ().map {
            ImageUiData(it, false)
        }
    }
}