package chad.orionsoft.sendit.ui.main_screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.NotificationDomain
import chad.orionsoft.sendit.domain.TcpServer
import chad.orionsoft.sendit.model.MediaData
import chad.orionsoft.sendit.repository.ConnectionRepository
import chad.orionsoft.sendit.utils.Converters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    @ApplicationContext private val context: Context,
    notificationDomain: NotificationDomain
) : ViewModel() {

    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainScreenState = _mainScreenState.asStateFlow()

    init {
        notificationDomain.showNotification(12, "SendIt", "Test notification")
        viewModelScope.launch {
            connectionRepository.connectionUpdateReceiver.collectLatest {
                _mainScreenState.update { state ->
                    state.copy(
                        connectedDeviceDetails = it
                    )
                }
                if(it != null) {
                    TcpServer.start(5555, context.contentResolver)
                    notificationDomain.showNotification(12, "TCP Server", "Started on 5555") // TODO move to work manager
                }
            }
        }
    }

    fun onEvent(onItemSelected: MainScreenEvent.OnItemSelected) {
        _mainScreenState.update {
            it.copy(
                selectionModel = updateSelectionUiModel(onItemSelected.mData, onItemSelected.isSelected)
            )
        }
    }

    private fun updateSelectionUiModel(mediaData: MediaData, addOrRemoved: Boolean) : SelectionUiModel {
        val newCount =if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedCount + 1
        } else {
            _mainScreenState.value.selectionModel.selectedCount - 1
        }
        val newSize = if(addOrRemoved) {
            _mainScreenState.value.selectionModel.selectedSize + mediaData.size
        } else {
            _mainScreenState.value.selectionModel.selectedSize - mediaData.size
        }
        val displaySize = Converters.formatDataString(newSize)
        return SelectionUiModel(newCount, newSize, displaySize)
    }
}