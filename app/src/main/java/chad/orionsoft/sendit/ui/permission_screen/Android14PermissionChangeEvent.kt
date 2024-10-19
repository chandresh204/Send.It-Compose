package chad.orionsoft.sendit.ui.permission_screen

import chad.orionsoft.sendit.ui.common_media_screen.PermissionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object Android14PermissionChangeEvent {

    private val _permissionChangeFlow = MutableSharedFlow<PermissionType>()
    val permissionFlow = _permissionChangeFlow.asSharedFlow()

    fun updateAndroid14PermissionState(permissionType: PermissionType) {
        CoroutineScope(Dispatchers.IO).launch {
            _permissionChangeFlow.emit(permissionType)
        }
    }
}