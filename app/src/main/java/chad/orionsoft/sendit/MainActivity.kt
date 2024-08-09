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
        /*val wM = WorkManager.getInstance(this)
        val tagF = "testWork"
        val workInfo = wM.getWorkInfosByTag(tagF).get()
        if(workInfo.isEmpty()) {
            val workRequest = OneTimeWorkRequestBuilder<TcpServerWorker>()
                .setInputData(Data.Builder().apply {
                    putInt(Constants.TCP_SERVER_WM_PORT, 1234)
                }.build())
                .addTag(tagF).build()
            wM.enqueue(workRequest)
        }*/
        setContent {
            SendItTheme {
                MainNavigation()
            }
        }
    }
}