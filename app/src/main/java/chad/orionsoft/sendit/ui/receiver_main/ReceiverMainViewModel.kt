package chad.orionsoft.sendit.ui.receiver_main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReceiverMainViewModel @Inject constructor(): ViewModel() {

    private val _receiverState = MutableStateFlow(ReceiverMainState())
    val receiverState = _receiverState.asStateFlow()



}