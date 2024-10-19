package chad.orionsoft.sendit.ui.permission_screen

sealed class PermissionEvents {
    data class OnPermissionObtained(val permissions: Map<String, Boolean>) : PermissionEvents()
    data object OpenAppSettings : PermissionEvents()
}