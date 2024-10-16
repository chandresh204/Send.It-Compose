package chad.orionsoft.sendit.ui.common_media_screen

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.PermissionDomain
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.repository.MediaRepository
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.utils.Converters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CommonMediaScreenViewModel.CommonMediaViewModelFactory::class)
class CommonMediaScreenViewModel @AssistedInject constructor(
    private val thumbnailDomain: ThumbnailDomain,
    private val mediaRepository: MediaRepository,
    private val permissionDomain: PermissionDomain,
    private val preferenceRepository: PreferenceRepository,
    @Assisted private val fileType: FileType,
): ViewModel() {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var isStillLoadingFromDevice = true

    val mediaList = mutableStateListOf<MediaUiData>()
    private var allMediaList: List<MediaData>? = null

    private val _screenState = MutableStateFlow(
        when(fileType)  {
            FileType.FileTypeImage -> CommonMediaState(viewMode = ViewMode.ByLastModifiedInGrid)
            FileType.FileTypeAudio -> CommonMediaState(viewMode = ViewMode.ByNameInList)
            FileType.FileTypeVideo -> CommonMediaState(viewMode = ViewMode.ByLastModifiedInGrid)
            FileType.FileTypeOther -> CommonMediaState(viewMode = ViewMode.ByNameInList)
        }
    )
    val screenState = _screenState.asStateFlow()

    //for list pagination
    private var currentPage = 0
    private val pageSize = 50
    private var endReached = false

    init {
        loadMediaFromContentResolver(false)
        viewModelScope.launch {
            MainScreenViewModel.clearAllSelectionReceiver.collect {
                if(it) {
                    clearAllSelection()
                }
            }
        }
    }

    fun loadMediaFromContentResolver(callingAfterGettingPermission: Boolean) {
        ioScope.launch {
            isStillLoadingFromDevice = true
            var permissionType1 : PermissionType = PermissionType.NoPermission
            allMediaList = when(fileType) {
                FileType.FileTypeImage -> {
                    val imageAccessPermission = permissionDomain.isImagesPermission()
                    permissionType1 = imageAccessPermission
                    if(imageAccessPermission == PermissionType.NoPermission) {
                        emptyList()
                    } else {
                        mediaRepository.getImages()
                    }
                }
                FileType.FileTypeAudio -> {
                    val audioAccessPermission = permissionDomain.isAudioPermission()
                    permissionType1 = audioAccessPermission
                    if(audioAccessPermission == PermissionType.NoPermission) {
                        emptyList()
                    } else {
                        mediaRepository.generateAudioList()
                    }
                }
                FileType.FileTypeVideo -> {
                    val videoAccessPermission = permissionDomain.isVideoPermission()
                    permissionType1 = videoAccessPermission
                    if(videoAccessPermission == PermissionType.NoPermission) {
                        emptyList()
                    } else {
                        mediaRepository.generateVideoList()
                    }
                }
                else -> mediaRepository.getImages()
            }
            isStillLoadingFromDevice = false
            _screenState.update {
                it.copy(
                    permissionType = permissionType1
                )
            }
            if(callingAfterGettingPermission) {
                resetFullList()
            }
        }
    }

    private fun resetFullList() {
        mediaList.clear()
        endReached = false
        currentPage = 0
        loadMoreItemsToDisplay()
    }

    private fun clearAllSelection() {
        for(media in mediaList) {
            media.selected = false
        }
    }

    fun loadMoreItemsToDisplay() {
        if (endReached) return
        val startIndex = currentPage * pageSize
        val endIndex = (currentPage+1) * pageSize
        currentPage++
        ioScope.launch {
            while (isStillLoadingFromDevice) {
                println("Waiting for list to be populated from device")
                delay(100)  // wait for list to be populated
            }
            for(i in startIndex until endIndex) {
                val media = try {
                    allMediaList!![i]
                } catch (_:Exception) {
                    endReached = true
                    break
                }
                mediaList.add(MediaUiData(media, false, null))
                println("$fileType >>> added in list at $i")
            }
            for(i in startIndex until endIndex) {
                val media = try {
                    mediaList[i]
                } catch (_:Exception) {
                    break
                }
                val newThumb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    thumbnailDomain.generateThumbnail(media.mediaData.uri)
                } else {
                    thumbnailDomain.generateThumbnail(
                        Converters.extractLastElementOfUri(media.mediaData.uri),
                        media.mediaData.pathData ?: "",
                        media.mediaData.mediaType
                    )
                }
                mediaList[i] = mediaList[i].copy(
                    thumbnailBitmap = newThumb
                )
                println("$fileType >>> thumb generated for $i")
            }
        }
    }

    fun changeItemSelection(index: Int) {
        mediaList[index] = mediaList[index].copy(
            selected = !mediaList[index].selected
        )
    }

    fun getRequiredPermissionString(): Array<String> = permissionDomain.getRequiredPermissionStringAsPerMediaType(fileType)

    fun getHowManyTimesFullAccessRequested() : Int = preferenceRepository.getHowManyTimesFullAccessRequested()

    fun addCountToFullAccessRequested() = preferenceRepository.addCountToFullAccessRequested()

    @AssistedFactory
    interface CommonMediaViewModelFactory {
        fun create(fileType: FileType) : CommonMediaScreenViewModel
    }
}