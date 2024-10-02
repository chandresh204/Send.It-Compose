package chad.orionsoft.sendit.ui.sender_file_select

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.ContentResolverDomain
import chad.orionsoft.sendit.ui.main_screen.MainScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileSelectViewModel @Inject constructor(
    private val contentResolverDomain: ContentResolverDomain
) : ViewModel() {

    private val _fState = MutableStateFlow(FileSelectState())
    val fState = _fState.asStateFlow()

    private val tag = "FileSelectViewModel"

    init {
        viewModelScope.launch {
            MainScreenViewModel.clearAllSelectionReceiver.collect {
                _fState.update {
                    it.copy(
                        selectFiles = emptyList()
                    )
                }
            }
        }
    }

    fun onEvent(event: FileSelectEvent) {
        when(event) {
            is FileSelectEvent.OnFileData -> {
                val newSelectionList = contentResolverDomain.processFileSelectionIntent(event.intent)
                val newUiList = fState.value.selectFiles.toMutableList()
                newSelectionList.forEach { mData ->
                    if(newUiList.find { it.uri ==  mData.uri} == null) {
                        event.addIfDataFound(mData) // callback to add item in main screen sending list
                        newUiList.add(mData)
                    }
                }
                Log.d(tag, "New Selection: ${newSelectionList.joinToString()}")
                _fState.update {
                    it.copy(
                        selectFiles = newUiList
                    )
                }
            }

            is FileSelectEvent.OnFileRemove -> {
                val iToRemove = fState.value.selectFiles.find { it.id == event.id }
                iToRemove?.let { toRemove ->
                    val newList = fState.value.selectFiles.toMutableList()
                    newList.remove(toRemove)
                    _fState.update {
                        it.copy(
                            selectFiles = newList
                        )
                    }
                }
            }
        }
    }
}