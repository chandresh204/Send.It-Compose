package chad.orionsoft.sendit.ui.settings

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.AppLogger
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val appLogger : AppLogger
): ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState(
        preferenceRepository.getUserNameFromPreferences() ?: "",
        preferenceRepository.getAppColorFromPreference()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                appColorList = RuntimeConstants.createAppColorList()
            )
        }
    }

    fun onEvent(event: SettingsEvents) {
        when(event) {
            is SettingsEvents.OnNameEdited -> {
                _uiState.update {
                    it.copy(
                        myName = event.newString
                    )
                }
            }

            is SettingsEvents.OnAppColorSelected -> {
                _uiState.update {
                    it.copy(
                        appColor = event.colorConstants
                    )
                }
            }

            SettingsEvents.OnSettingSave -> {
                preferenceRepository.setUserNameInPreferences(uiState.value.myName)
                preferenceRepository.setAppColorInPreferences(uiState.value.appColor)
            }

            is SettingsEvents.OnDeveloperOptionChanged -> {
                _uiState.update {
                    it.copy(
                        isDeveloperOptions = event.isChecked
                    )
                }
            }

            SettingsEvents.OnShareLogs -> {
                appLogger.sendLogFile()
            }
        }
    }
}