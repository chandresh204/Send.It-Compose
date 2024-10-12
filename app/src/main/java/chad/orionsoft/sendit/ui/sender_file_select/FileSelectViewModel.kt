package chad.orionsoft.sendit.ui.sender_file_select

import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.ContentResolverDomain
import chad.orionsoft.sendit.domain.ThumbnailDomain
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import chad.orionsoft.sendit.ui.model.MediaUiData
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileSelectViewModel @Inject constructor(
    private val contentResolverDomain: ContentResolverDomain,
    private val thumbnailDomain: ThumbnailDomain
) : ViewModel() {

    private val tag = "FileSelectViewModel"

    val selectedMedia = mutableStateListOf<MediaUiData>()
    init {
        viewModelScope.launch {
            MainScreenViewModel.clearAllSelectionReceiver.collect {
                selectedMedia.clear()
            }
        }
    }

    fun onEvent(event: FileSelectEvent) {
        when(event) {
            is FileSelectEvent.OnFileData -> {
                val newSelectionList = contentResolverDomain.processFileSelectionIntent(event.intent)
                newSelectionList.forEach { mData ->
                    if(selectedMedia.find { it.mediaData.uri ==  mData.uri} == null) {
                        event.addIfDataFound(mData) // callback to add item in main screen sending list
                        val newMUIData = MediaUiData(mData, true, null)
                        selectedMedia.add(newMUIData)
                        // launch the thumbnail generate process async for this file
                        viewModelScope.launch {
                            val thumbBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                thumbnailDomain.generateThumbnail(mData.uri)
                            } else {
                                thumbnailDomain.generateThumbnail(Converters.extractLastElementOfUri(mData.uri),
                                    mData.pathData ?: "", mData.mediaType)
                            }
                            thumbBitmap?.let {
                                val indexOf = selectedMedia.indexOf(newMUIData)
                                if(indexOf >= 0) {
                                    selectedMedia[indexOf] = selectedMedia[indexOf].copy(
                                        thumbnailBitmap = it
                                    )
                                }
                            }
                        }

                    }
                }
            }

            is FileSelectEvent.OnFileRemove -> {
                selectedMedia.removeAt(event.index)
            }
        }
    }
}