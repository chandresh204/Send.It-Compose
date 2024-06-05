package chad.orionsoft.sendit.ui.sender_image

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SenderImagesViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(SenderImagesState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                images = createDummyData()
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

    private fun changeSelectionStateOfImage(id: Int) {
        val updatedList = mutableListOf<ImageData>()
        updatedList.addAll(uiState.value.images)
        val img = updatedList.find { it.id == id }
        img?.let {  img1 ->
            img1.isSelected = !img1.isSelected
        }
        _uiState.update {
            it.copy(
                images = updatedList
            )
        }
    }

    private fun createDummyData(): List<ImageData> {
        return listOf(
            ImageData(1, Uri.parse(""), false),
            ImageData(2, Uri.parse(""), false),
            ImageData(3, Uri.parse(""), true),
            ImageData(4, Uri.parse(""), false)
        )
    }
}