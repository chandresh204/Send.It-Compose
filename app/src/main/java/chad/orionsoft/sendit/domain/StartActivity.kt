package chad.orionsoft.sendit.domain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Converters

object StartActivity {

    fun openTetherSettings(context: Context) {
        val i= Intent(Intent.ACTION_MAIN,null)
        val cName= ComponentName("com.android.settings","com.android.settings.TetherSettings")
        i.component = cName
        context.startActivity(i)
        Toast.makeText(context,context.getString(R.string.turn_on_hotspot),Toast.LENGTH_LONG).show()
    }

    fun openWifiSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    fun openFile(context: Context, filename: String, uri: Uri?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, Converters.getMimeTypeFromFileName(filename))
        }
        context.startActivity(intent)
    }
}