package chad.orionsoft.sendit.ui.welcome_screen

import chad.orionsoft.sendit.utils.ColorConstants

data class WelcomeState(
    val userName: String = "",
    val appColor: ColorConstants,
    val appColorList : List<ColorConstants> = emptyList()
)