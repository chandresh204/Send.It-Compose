package chad.orionsoft.sendit.ui.transfer_process_screen.transfer_sending

import chad.orionsoft.sendit.ui.transfer_process_screen.model.TransferItemUi

data class TransferSendingState(
    val sentList: List<TransferItemUi> = emptyList(),
)