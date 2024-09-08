package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_receiving

import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferItemUi

data class TransferReceivingState(
    val receiveList: List<TransferItemUi> = emptyList(),
)