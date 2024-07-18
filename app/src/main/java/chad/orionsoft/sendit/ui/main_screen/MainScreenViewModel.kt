package chad.orionsoft.sendit.ui.main_screen

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor() : ViewModel() {

    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainScreenState = _mainScreenState.asStateFlow()

    fun onEvent(onItemSelected: MainScreenEvent.OnItemSelected) {
        _mainScreenState.update {
            it.copy(
                selectionModel = updateSelectionUiModel(onItemSelected.mData, onItemSelected.isSelected)
            )
        }
    }

    private fun updateSelectionUiModel(mediaData: MediaData, addOrRemoved: Boolean) : SelectionUiModel {
        val newCount =if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedCount + 1
        } else {
            _mainScreenState.value.selectionModel.selectedCount - 1
        }
        val newSize = if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedSize + mediaData.size
        } else {
            _mainScreenState.value.selectionModel.selectedSize - mediaData.size
        }
        val displaySize = Converters.formatDataString(newSize)
        return SelectionUiModel(newCount, newSize, displaySize)
    }
}