package chad.orionsoft.sendit.ui.edit_screen

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditScreenViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _editScreenState = MutableStateFlow(EditScreenState(
        preferenceRepository.getUserNameFromPreferences() ?: ""
    ))

    val editScreenState = _editScreenState.asStateFlow()

    fun onEvent(event: EditScreenEvent) {
        when(event) {
            is EditScreenEvent.OnSaveClicked -> {
                preferenceRepository.setUserNameInPreferences(editScreenState.value.userName)
            }
            is EditScreenEvent.OnUseNameUpdated -> {
                _editScreenState.update {
                    it.copy(
                        userName = event.new
                    )
                }
            }
        }
    }
}