package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import chad.orionsoft.sendit.model.FileType
import chad.orionsoft.sendit.ui.common_media_screen.PermissionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * return array of permission results arrayOf(full access permission, partial access permission)
     */
    fun isImagesPermission() : PermissionType {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) -> PermissionType.FullAccess
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> PermissionType.PartialAccess
                    else -> PermissionType.NoPermission
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
            else -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
        }
    }

    fun isAudioPermission() : PermissionType {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
            else -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
        }
    }

    fun isVideoPermission() : PermissionType {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) -> PermissionType.FullAccess
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> PermissionType.PartialAccess
                    else -> PermissionType.NoPermission
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
            else -> {
                return when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> PermissionType.FullAccess
                    else -> PermissionType.NoPermission
                }
            }
        }
    }

    fun getRequiredPermissionStringAsPerMediaType(fileType: FileType) : Array<String> {
        return when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> {
                when(fileType) {
                    FileType.FileTypeImage -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    FileType.FileTypeAudio -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
                    }
                    FileType.FileTypeVideo -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO)
                    }
                    else -> {
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val visualSelectedPermission = android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                when(fileType) {
                    FileType.FileTypeImage -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, visualSelectedPermission)
                    }
                    FileType.FileTypeAudio -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
                    }
                    FileType.FileTypeVideo -> {
                        arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO, visualSelectedPermission)
                    }
                    else -> {
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
            else -> {
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    fun getAllMediaPermissionsString() : Array<String> {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}