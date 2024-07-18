package chad.orionsoft.sendit.ui.main_screen

data class MainScreenState(
    val selectionModel : SelectionUiModel = SelectionUiModel()
)

data class SelectionUiModel(
    val selectedCount : Int = 0,
    val selectedSize : Long = 0,
    val displaySize : String = "0B"
)