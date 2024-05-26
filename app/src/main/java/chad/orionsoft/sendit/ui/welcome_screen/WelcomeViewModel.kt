package chad.orionsoft.sendit.ui.welcome_screen

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeState())
    val uiState = _uiState.asStateFlow()

    init {
        if(!preferenceRepository.getUserNameFromPreferences().isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    moveToNextScreen = true
                )
            }
        }
    }

    fun onEvent(welcomeEvents: WelcomeEvents) {
        when(welcomeEvents) {
            is WelcomeEvents.OnNextClick -> {
                preferenceRepository.setUserNameInPreferences(uiState.value.userName ?: "")
                _uiState.update {
                    it.copy(
                        moveToNextScreen = true
                    )
                }
            }

            is WelcomeEvents.OnUserNameChanged -> {
                _uiState.update {
                    it.copy(
                        userName = welcomeEvents.new
                    )
                }
            }
        }
    }

}