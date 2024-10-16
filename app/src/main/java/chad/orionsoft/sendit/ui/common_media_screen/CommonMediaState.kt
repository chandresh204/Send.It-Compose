package chad.orionsoft.sendit.ui.common_media_screen

data class CommonMediaState(
    val viewMode : ViewMode,
    val permissionType: PermissionType = PermissionType.FullAccess,
)

enum class ViewMode {
    ByNameInList, ByLastModifiedInGrid
}

enum class PermissionType {
    FullAccess, PartialAccess, NoPermission
}