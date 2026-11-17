package chad.orionsoft.sendit.ui.settings

import chad.orionsoft.sendit.utils.ColorConstants

data class SettingsState(
    val myName: String,
    val appColor: ColorConstants,
    val appColorList : List<ColorConstants> = emptyList(),
    val isDeveloperOptions: Boolean = false
)