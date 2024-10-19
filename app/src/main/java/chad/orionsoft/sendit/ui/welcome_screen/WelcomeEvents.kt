package chad.orionsoft.sendit.ui.welcome_screen

import chad.orionsoft.sendit.utils.ColorConstants

sealed class WelcomeEvents {
    data object OnSaveUsername : WelcomeEvents()
    data class OnUserNameChanged(val new: String) :WelcomeEvents()
    data class OnNewColorSelected(val color: ColorConstants) : WelcomeEvents()
    data object OnSaveAppColor : WelcomeEvents()
    data class OnPermissionObtained(val permissions: Map<String,Boolean>) : WelcomeEvents()
}