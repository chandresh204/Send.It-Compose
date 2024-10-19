package chad.orionsoft.sendit.ui.main_screen

import chad.orionsoft.sendit.model.ConnectedDeviceDetails

data class MainScreenState(
    val selectionModel : SelectionUiModel = SelectionUiModel(),
    val connectedDeviceDetails: ConnectedDeviceDetails? = null,
    val sendingFlowInfo : TransferFlowDetails = TransferFlowDetails(),
    val receivingFlowInfo : TransferFlowDetails = TransferFlowDetails(),
    val totalSentAndReceived: Pair<String, String> = Pair("0B", "0B")
)

data class SelectionUiModel(
    val selectedCount : Int = 0,
    val selectedSize : Long = 0,
    val displaySize : String = "0B/1.4G",
    val isWarning : Boolean = false,
)

data class TransferFlowDetails(
    val isVisible:Boolean = false,
    val transferRate : String = "-1",
    val transferRateUnit: String = "B/s",
    val transferItemDetail: Array<String> = Array(2) { "" },
    val currentItemProgress: Float = 0f
)