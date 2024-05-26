package chad.orionsoft.sendit.ui.welcome_screen

sealed class WelcomeEvents {
    data object OnNextClick : WelcomeEvents()
    data class OnUserNameChanged(val new: String) :WelcomeEvents()

}