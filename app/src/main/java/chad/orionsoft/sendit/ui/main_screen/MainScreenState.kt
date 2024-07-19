package chad.orionsoft.sendit.ui.main_screen

import chad.orionsoft.sendit.model.ConnectedDeviceDetails

data class MainScreenState(
    val selectionModel : SelectionUiModel = SelectionUiModel(),
    val connectedDeviceDetails: ConnectedDeviceDetails? = null
)

data class SelectionUiModel(
    val selectedCount : Int = 0,
    val selectedSize : Long = 0,
    val displaySize : String = "0B"
)