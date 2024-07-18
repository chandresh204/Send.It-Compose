package chad.orionsoft.sendit.ui.receiver_main

import androidx.lifecycle.ViewModel
import chad.orionsoft.sendit.domain.ReceiverTCP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReceiverMainViewModel @Inject constructor(
    private val receiverTCP: ReceiverTCP
): ViewModel() {

    private val _receiverState = MutableStateFlow(ReceiverMainState())
    val receiverState = _receiverState.asStateFlow()

    fun onEvent(event: ReceiverMainEvent) {
        when(event) {
            ReceiverMainEvent.StartReceiver -> {
                receiverTCP.startReceiving()
            }
        }
    }
}