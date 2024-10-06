package chad.orionsoft.sendit.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.utils.ColorConstants
import chad.orionsoft.sendit.utils.RuntimeConstants
import chad.orionsoft.sendit.utils.SharedPreferencesConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun addLog(msg: String) {
        Log.d("Preferences", msg)
    }

    private val sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.APP_PREFS_NAMES, MODE_PRIVATE)

    fun setUserNameInPreferences(name: String) {
        sharedPreferences.edit().putString(
            SharedPreferencesConstants.PREFS_USER_NAME, name
        ).apply()
    }

    fun getUserNameFromPreferences() : String? =
        sharedPreferences.getString(SharedPreferencesConstants.PREFS_USER_NAME, null)

    fun setAppColorInPreferences(appColor: ColorConstants) {
        sharedPreferences.edit().putInt(
            SharedPreferencesConstants.PREFS_APP_COLOR, appColor.id
        ).apply()
    }

    fun getAppColorFromPreference() : ColorConstants {
        val colorId = sharedPreferences.getInt(SharedPreferencesConstants.PREFS_APP_COLOR, ColorConstants.Device.id)
        val fromPrefs = ColorConstants.entries.find { it.id == colorId }
        return fromPrefs ?: ColorConstants.Device
    }

    fun getKnownReceivers() : MutableSet<String>? =
        sharedPreferences.getStringSet(SharedPreferencesConstants.PREFS_KNOWN_RECEIVER, emptySet())

    fun getMyColorCode() : Color {
        return Color.Unspecified.copy(
            red = 0.8f, green = 0.2f,
            blue = 0.4f
        )
    }
}