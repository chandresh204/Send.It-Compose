package chad.orionsoft.sendit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import chad.orionsoft.sendit.R

object IntentUtil {

    fun getAppSharingIntent(context: Context) : Intent {
        val shareMsg = context.getString(R.string.share_app_message) +
                "\n${Constants.PLAYSTORE_URL}"
        return Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareMsg)
            type = "text/plain"
        }
    }

    fun appPlayStoreIntent() : Intent{
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(Constants.PLAYSTORE_URL)
        return i
    }
}