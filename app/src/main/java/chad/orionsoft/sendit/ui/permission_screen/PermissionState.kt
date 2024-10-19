package chad.orionsoft.sendit.ui.permission_screen

import chad.orionsoft.sendit.ui.common_media_screen.PermissionType

data class PermissionState(
    val isAudioPermission : Boolean = false,
    val avPermissionType: PermissionType = PermissionType.NoPermission,
    val imageAccessedCount: Int = 0,
    val audioAccessCount: Int = 0,
    val videoAccessCount: Int = 0
)