package chad.orionsoft.sendit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import chad.orionsoft.sendit.repository.PreferenceRepository
import chad.orionsoft.sendit.ui.MainNavigation
import chad.orionsoft.sendit.ui.theme.SendItTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedIntent = if(intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
            // navigate directly to file select tab and show shared items
            intent
        } else null
        setContent {
            var selectedAppColor by remember {
                mutableStateOf(preferenceRepository.getAppColorFromPreference())
            }
            SendItTheme(selectedAppColor) {
                MainNavigation(sharedIntent) {
                    selectedAppColor = it
                }
//                CommonMediaScreenWithViewModel()
            }
        }
    }
}