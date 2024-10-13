package chad.orionsoft.sendit.ui.common_media_screen

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.PermissionDomain
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.repository.MediaRepository
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
    @Assisted private val fileType: FileType,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var isStillLoadingFromDevice = true

    val mediaList = mutableStateListOf<MediaUiData>()
    private var allMediaList: List<MediaData>? = null
    val isMissingPermission = MutableStateFlow(false)

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
    }

    fun loadMediaFromContentResolver(callingAfterGettingPermission: Boolean) {
        ioScope.launch {
            isStillLoadingFromDevice = true
            var isMissingPermission1 = false
            allMediaList = when(fileType) {
                FileType.FileTypeImage -> if(permissionDomain.isImagesPermission()) {
                    isMissingPermission1 = false
                    mediaRepository.getImages()
                } else {
                    isMissingPermission1 = true
                    emptyList()
                }
                FileType.FileTypeAudio -> if(permissionDomain.isAudioPermission()) {
                    isMissingPermission1 = false
                    mediaRepository.generateAudioList()
                } else {
                    isMissingPermission1 = true
                    emptyList()
                }
                FileType.FileTypeVideo -> if(permissionDomain.isVideoPermission()) {
                    isMissingPermission1 = false
                    mediaRepository.generateVideoList()
                } else {
                    isMissingPermission1 = true
                    emptyList()
                }
                else -> mediaRepository.getImages()
            }
            isMissingPermission.update {
                isMissingPermission1
            }
            isStillLoadingFromDevice = false
            if(callingAfterGettingPermission) {
                loadMoreItemsToDisplay()
            }
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
                println("$fileType >>> added in list at $startIndex")
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
                println("$fileType >>> thumb generated for $startIndex")
            }
        }
    }

    fun changeItemSelection(index: Int) {
        mediaList[index] = mediaList[index].copy(
            selected = !mediaList[index].selected
        )
    }

    fun getRequiredPermissionString(): String = permissionDomain.getRequiredPermissionStringAsPerMediaType(fileType)

    @AssistedFactory
    interface CommonMediaViewModelFactory {
        fun create(fileType: FileType) : CommonMediaScreenViewModel
    }
}