package chad.orionsoft.sendit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import chad.orionsoft.sendit.ui.MainNavigation
import chad.orionsoft.sendit.ui.theme.SendItTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SendItTheme {
                MainNavigation()
            }
        }
    }
}