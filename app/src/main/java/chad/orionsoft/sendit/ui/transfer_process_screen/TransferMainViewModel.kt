package chad.orionsoft.sendit.ui.transfer_process_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chad.orionsoft.sendit.domain.TransferProcessDomain
import chad.orionsoft.sendit.utils.Converters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferMainViewModel : ViewModel() {

    private val _tMainState = MutableStateFlow(TransferMainState())
    val tMainState = _tMainState.asStateFlow()

    init {
        receiveSendingSpeedEvent()
        receiveSendingCountEvent()
    }


    private fun receiveSendingSpeedEvent() {
        viewModelScope.launch {
            TransferProcessDomain.transferSpeedFlow.collectLatest { sentBytes ->
                _tMainState.update {
                    it.copy(
                        sendingSpeed = Converters.formatDataString(sentBytes.first),
                        receivingItemSpeed = Converters.formatDataString(sentBytes.second),
                        totalSentAndReceived = Pair(
                            Converters.formatDataString(TransferProcessDomain.getTotalSentInSession()),
                            Converters.formatDataString(TransferProcessDomain.getTotalReceivedInSession())
                        )
                    )
                }
            }

        }
    }

    private fun receiveSendingCountEvent() {
        viewModelScope.launch {
            TransferProcessDomain.sendingItemCountFlow.collectLatest { countNTotal ->
                _tMainState.update {
                    it.copy(
                        sendingItemCount = "${countNTotal.first}/${countNTotal.second}"
                    )
                }
            }
        }
    }

}