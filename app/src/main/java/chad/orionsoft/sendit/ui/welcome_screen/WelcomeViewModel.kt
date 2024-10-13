package chad.orionsoft.sendit.ui.welcome_screen

import android.os.Build
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.PermissionDomain
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.RuntimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val permissionDomain: PermissionDomain
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeState(
        appColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ColorConstants.Device
        } else ColorConstants.Purple,
        appColorList = RuntimeConstants.createAppColorList()
    ))
    val uiState = _uiState.asStateFlow()

    val tag = "WelcomeViewModel"
    init {
        _uiState.update {
            it.copy(
                userName = preferenceRepository.getUserNameFromPreferences() ?: ""
            )
        }
    }

    fun onEvent(welcomeEvents: WelcomeEvents) {
        when(welcomeEvents) {
            is WelcomeEvents.OnSaveUsername -> {
                preferenceRepository.setUserNameInPreferences(uiState.value.userName)
            }

            is WelcomeEvents.OnUserNameChanged -> {
                _uiState.update {
                    it.copy(
                        userName = welcomeEvents.new
                    )
                }
            }

            is WelcomeEvents.OnNewColorSelected -> {
                _uiState.update {
                    it.copy(
                        appColor = welcomeEvents.color
                    )
                }
            }

            WelcomeEvents.OnSaveAppColor -> {
                preferenceRepository.setAppColorInPreferences(uiState.value.appColor)
            }
        }
    }

    fun getAllRequiredMediaPermissionArray() : Array<String> = permissionDomain.getAllMediaPermissionsString()

}