package chad.orionsoft.sendit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.MainNavigation
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.NavigationConstants
import chad.orionsoft.sendit.utils.RuntimeConstants
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferenceRepository: PreferenceRepository

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdates()
        enableEdgeToEdge()
        val sharedIntent = if(intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
            // navigate directly to file select tab and show shared items
            intent
        } else null
        setContent {
            var selectedAppColor by remember {
                mutableStateOf(preferenceRepository.getAppColorFromPreference())
            }
            val userName = preferenceRepository.getUserNameFromPreferences()
            SendItTheme(selectedAppColor) {
                RuntimeConstants.myColor = selectedAppColor.color ?: MaterialTheme.colorScheme.primary
                MainNavigation(sharedIntent = sharedIntent,
                    initScreen = if(userName.isNullOrEmpty()) NavigationConstants.NAV_WELCOME_SCREEN else NavigationConstants.NAV_HOME_SCREEN,
                    closeApp = {
                        this.finishAffinity()
                    })
                {
                    selectedAppColor = it
                }
            }
        }
    }

    private fun checkForAppUpdates() {
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
        }
        appUpdateInfoTask.addOnFailureListener {
            Log.e("In-App-Update", "Failed: $it")
        }
    }

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val tag = "In-App-Updates"
        Log.d(tag, ">>>> result: $result")
    }

}