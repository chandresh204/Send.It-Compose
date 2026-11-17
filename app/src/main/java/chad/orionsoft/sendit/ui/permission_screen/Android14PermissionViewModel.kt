package chad.orionsoft.sendit.ui.permission_screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.AppLogger
import chad.orionsoft.sendit.domain.PermissionDomain
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.ui.common_media_screen.PermissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class Android14PermissionViewModel @Inject constructor(
    private val permissionDomain: PermissionDomain,
    private val mediaRepository: MediaRepository,
    private val appLogger: AppLogger
) : ViewModel() {

    private val _pState = MutableStateFlow(PermissionState())
    val pState = _pState.asStateFlow()

    private val tag = "PermissionViewModel"

    init {
        val isAudioPermission = permissionDomain.isAudioPermission() != PermissionType.NoPermission
        val isVideoPermission = permissionDomain.isVideoPermission()
        _pState.update {
            it.copy(
                isAudioPermission = isAudioPermission,
                avPermissionType = isVideoPermission
            )
        }
        if(isAudioPermission && isVideoPermission != PermissionType.NoPermission) {
            Android14PermissionChangeEvent.updateAndroid14PermissionState(PermissionType.PartialAccess)
            obtainMediaCountsIfPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun onEvent(event: PermissionEvents) {
        when(event) {
            is PermissionEvents.OnPermissionObtained -> {
                Log.d(tag, "${event.permissions}")
                val audioPermission = event.permissions[android.Manifest.permission.READ_MEDIA_AUDIO]
                val imagePermission = event.permissions[android.Manifest.permission.READ_MEDIA_IMAGES]
                val videoPermission = event.permissions[android.Manifest.permission.READ_MEDIA_VIDEO]
                val partialAccessPermission = event.permissions[android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED]
                _pState.update {
                    it.copy(
                        isAudioPermission = audioPermission ?: false,
                        avPermissionType = if(imagePermission == true && videoPermission == true) {
                            PermissionType.FullAccess
                        } else if (partialAccessPermission == true) {
                            PermissionType.PartialAccess
                        } else PermissionType.NoPermission
                    )
                }
                appLogger.log("Permission event : isAudio : $audioPermission, partialAccessPermission: $partialAccessPermission")
                if(audioPermission == true && partialAccessPermission == true) {
                    Android14PermissionChangeEvent.updateAndroid14PermissionState(PermissionType.PartialAccess)
                    obtainMediaCountsIfPermission()
                } else {
                    Android14PermissionChangeEvent.updateAndroid14PermissionState(PermissionType.NoPermission)
                }
            }
            is PermissionEvents.OpenAppSettings -> {
                permissionDomain.openAppSettings()
            }
        }
    }

    private fun obtainMediaCountsIfPermission() {
        val images = mediaRepository.getImages()
        val audio = mediaRepository.generateAudioList()
        val video = mediaRepository.generateVideoList()
        _pState.update {
            it.copy(
                imageAccessedCount = images.size,
                audioAccessCount = audio.size,
                videoAccessCount = video.size
            )
        }
    }

    fun getAllRequiredPermissionArray() : Array<String> =
        permissionDomain.getAllMediaPermissionsString()
}