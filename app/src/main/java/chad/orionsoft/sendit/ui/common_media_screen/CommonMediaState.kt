package chad.orionsoft.sendit.ui.common_media_screen

data class CommonMediaState(
    val viewMode : ViewMode,
)

enum class ViewMode {
    ByNameInList, ByLastModifiedInGrid
}