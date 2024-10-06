package chad.orionsoft.sendit.ui.settings

import chad.orionsoft.sendit.utils.ColorConstants

sealed class SettingsEvents {
    data class OnNameEdited(val newString: String) : SettingsEvents()
    data class OnAppColorSelected(val colorConstants: ColorConstants) : SettingsEvents()
    data object OnSettingSave : SettingsEvents()
}