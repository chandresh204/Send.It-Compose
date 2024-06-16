package chad.orionsoft.sendit.ui.sender_file_select

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FileSelectViewModel @Inject constructor() : ViewModel() {

    private val _fState = MutableStateFlow(FileSelectState())
    val fState = _fState.asStateFlow()

    fun onEvent(event: FileSelectEvent) {
        when(event) {
            FileSelectEvent.OnAddNewClicked -> {
                // TODO open file select
            }
        }
    }
}