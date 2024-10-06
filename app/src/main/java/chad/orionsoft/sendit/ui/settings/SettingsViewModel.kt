package chad.orionsoft.sendit.ui.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.ColorConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState(
        preferenceRepository.getUserNameFromPreferences() ?: "",
        preferenceRepository.getAppColorFromPreference()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        createAppColorList()
    }

    private fun createAppColorList() {
        val appColors = mutableListOf<ColorConstants>()
        ColorConstants.entries.forEach {
            if(it == ColorConstants.Device && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return@forEach
            }
            appColors.add(it)
        }
        _uiState.update {
            it.copy(
                appColorList = appColors
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
        }
    }
}