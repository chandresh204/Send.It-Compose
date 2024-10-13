package chad.orionsoft.sendit.domain

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import chad.orionsoft.sendit.model.FileType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionDomain @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isImagesPermission() : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isAudioPermission() : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isVideoPermission() : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissionStringAsPerMediaType(fileType: FileType) : String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when(fileType) {
                FileType.FileTypeImage -> {
                    android.Manifest.permission.READ_MEDIA_IMAGES
                }
                FileType.FileTypeAudio -> {
                    android.Manifest.permission.READ_MEDIA_AUDIO
                }
                FileType.FileTypeVideo -> {
                    android.Manifest.permission.READ_MEDIA_VIDEO
                }
                else -> {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
            }
        } else {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    }

    fun getAllMediaPermissionsString() : Array<String> {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            emptyArray()
        }
    }
}